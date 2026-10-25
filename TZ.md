# ТЗ: Семестровая работа №1 — «Tradance», криптобиржа (стакан заявок + секретная фраза)

Дедлайн: **уточнить у преподавателя** (сдача лично, с показом кода и ответами на вопросы)
Проект: `~/IdeaProjects/Tradance` (Maven, `war`, Tomcat 9.0.122 → `javax.*`, JDK 25)
Пакет: `ru.itis.*`, БД: PostgreSQL `Tradance`

> ⚠️ По правилам курса код на Java, JS и SQL генерировать нельзя. Поэтому здесь только **контракты**: таблицы описаны словами (колонка, тип, ограничение), классы описаны сигнатурами и поведением. SQL и реализацию пишешь сам.

---

## 0. Что уже изучено и что нужно доучить

### Берём из `ClassWork_26Sept` и `HwOct24`

| Тема | Где было | Где используем в семестровке |
|---|---|---|
| Слои `models → repository → services → servlets` | ClassWork, HwOct24 | весь проект |
| `CrudRepository<T>` + интерфейс на сущность + `...JdbcImpl` | `repository/` | 6 репозиториев |
| JDBC: `DataSource`, `PreparedStatement`, `ResultSet`, try-with-resources | `UsersRepositoryJdbcImpl` | все репозитории |
| `DriverManagerDataSource` (`spring-jdbc`) | listener | listener (только DataSource, **без JdbcTemplate**) |
| `@WebListener` + константы имён атрибутов | `AppContextListener` в HwOct24 | `AppContextListener` |
| `init(ServletConfig)` → сервис из `ServletContext` | сервлеты | все сервлеты |
| PRG: `doGet` → forward, успешный `doPost` → `sendRedirect` | `SaveUsersServlet` | все формы |
| JSP + JSTL (`c:forEach`, `c:if`) | `users.jsp`, `save.jsp` | все страницы |
| DTO-форма отдельно от модели | `SignUpForm`, `UserForm` | все формы |
| BCrypt (`PasswordEncoder`) | `SignUpServiceImpl` | пароль и слова фразы |
| Session | `UsersServlets` | вход, 2 шага логина, flash-сообщения |
| AJAX + Jackson | `AjaxServlet`, `ajax_example.html` | стакан, заявки, избранное |
| `db.properties` из `resources` | HwOct24 | настройки БД и папки загрузок |
| `web.xml` с `request-character-encoding` | HwOct24 | кодировка |

### Нового на парах не было, изучить по ходу

| Тема | Зачем |
|---|---|
| `@WebFilter`, `FilterChain` | фильтры входа и ролей (требование ТЗ) |
| `@MultipartConfig`, `Part` | загрузка аватаров и иконок |
| Свои теги `.tag` (`WEB-INF/tags`), `<jsp:doBody/>` | система шаблонов (требование ТЗ) |
| JDBC-транзакции: `setAutoCommit(false)`, `commit()`, `rollback()` | исполнение сделки «всё или ничего». **Уточнить у препода** |
| `BigDecimal` и `NUMERIC` | деньги без ошибок округления |
| `SecureRandom` | генерация секретной фразы |
| `fetch` + JSON в JS | AJAX без перезагрузки |

### Ошибки, которые нельзя переносить из классной работы
1. Не мапить ничего на `@WebServlet("/")`, иначе перестанет отдаваться статика.
2. Имена атрибутов контекста вынести в константы.
3. Пароль от БД не хардкодить, брать из `db.properties`.
4. Не использовать сырые типы (`List` без `<...>`).
5. `Connection` всегда открывать в try-with-resources.

---

## 1. Постановка задачи

Учебная биржа. Пользователи торгуют монетами **друг с другом** через стакан лимитных заявок. Котируемая валюта одна: `USDT`. Все пары имеют вид `X/USDT`.

**Роли**
- **Гость:** рынок (список монет и цены), страница пары (стакан и последние сделки только для просмотра), регистрация, вход, восстановление пароля по фразе.
- **Пользователь (`USER`):** всё, что доступно гостю, плюс кошельки, выставление и отмена заявок, «Мои заявки», «Мои сделки», профиль с аватаром, избранные монеты.
- **Админ (`ADMIN`):** всё, что доступно пользователю, плюс админка: CRUD монет, пользователи (блокировка, тестовое пополнение), все заявки и сделки.

**Упрощения (зафиксированы):** только лимитные заявки, без комиссий, одна котируемая валюта, матчинг выполняется синхронно в момент выставления заявки.

**Стартовый баланс:** при регистрации пользователю создаётся кошелёк `USDT` с балансом **10 000**. Монеты появляются только через покупку или тестовое пополнение от админа.

**Первый админ:** зарегистрироваться обычным способом, потом в БД руками выполнить `update` роли на `ADMIN`. Хэш пароля через SQL не сделать, поэтому такой путь.

---

## 2. База данных

Скрипты лежат в `sql/` в корне проекта (в war не попадают): `01_schema.sql`, `02_seed.sql`.
Деньги и количества хранятся только в `NUMERIC(30, 8)`, никогда не в `real`/`double`.

### 2.1 `users`
| Колонка | Тип | Ограничения |
|---|---|---|
| `id` | `bigserial` | PK |
| `username` | `varchar(30)` | not null, unique |
| `email` | `varchar(255)` | not null, unique |
| `password_hash` | `varchar(100)` | not null (BCrypt) |
| `role` | `varchar(10)` | not null, default `'USER'`, check: `USER` или `ADMIN` |
| `is_blocked` | `boolean` | not null, default `false` |
| `avatar_path` | `varchar(255)` | null (имя файла, не полный путь) |
| `created_at` | `timestamp` | not null, default `now()` |

### 2.2 `recovery_words`: секретная фраза
| Колонка | Тип | Ограничения |
|---|---|---|
| `id` | `bigserial` | PK |
| `user_id` | `bigint` | FK → `users.id`, `on delete cascade`, not null |
| `position` | `smallint` | not null, check `1..10` |
| `word_hash` | `varchar(100)` | not null (BCrypt одного слова) |
| | | unique (`user_id`, `position`) |

Связь: **O2M** `users` → `recovery_words`, у каждого пользователя ровно 10 строк.

### 2.3 `currencies`
| Колонка | Тип | Ограничения |
|---|---|---|
| `id` | `bigserial` | PK |
| `ticker` | `varchar(10)` | not null, unique (`BTC`, `ETH`, `USDT`) |
| `name` | `varchar(50)` | not null |
| `icon_path` | `varchar(255)` | null |
| `is_quote` | `boolean` | not null, default `false` (`true` только у `USDT`) |
| `is_active` | `boolean` | not null, default `true` (торговля разрешена) |

### 2.4 `wallets`
| Колонка | Тип | Ограничения |
|---|---|---|
| `id` | `bigserial` | PK |
| `user_id` | `bigint` | FK → `users.id`, not null |
| `currency_id` | `bigint` | FK → `currencies.id`, not null |
| `balance` | `numeric(30,8)` | not null, default 0, check `>= 0` |
| `locked` | `numeric(30,8)` | not null, default 0, check `>= 0` и `locked <= balance` |
| | | unique (`user_id`, `currency_id`) |

Доступно = `balance − locked`. Связи **O2M**: `users` → `wallets` и `currencies` → `wallets`.

### 2.5 `orders`
| Колонка | Тип | Ограничения |
|---|---|---|
| `id` | `bigserial` | PK |
| `user_id` | `bigint` | FK → `users.id`, not null |
| `currency_id` | `bigint` | FK → `currencies.id`, not null (базовая монета пары) |
| `side` | `varchar(4)` | not null, check: `BUY` или `SELL` |
| `price` | `numeric(30,8)` | not null, check `> 0` (цена за 1 монету в USDT) |
| `amount` | `numeric(30,8)` | not null, check `> 0` (сколько монет хотел) |
| `filled` | `numeric(30,8)` | not null, default 0, check `0 <= filled <= amount` |
| `status` | `varchar(10)` | not null, check: `OPEN`, `PARTIAL`, `FILLED`, `CANCELLED` |
| `created_at` | `timestamp` | not null, default `now()` |

Индекс на (`currency_id`, `side`, `status`, `price`): по нему ищется стакан.

### 2.6 `trades`: таблица с 3+ FK
| Колонка | Тип | Ограничения |
|---|---|---|
| `id` | `bigserial` | PK |
| `buy_order_id` | `bigint` | FK → `orders.id`, not null |
| `sell_order_id` | `bigint` | FK → `orders.id`, not null |
| `buyer_id` | `bigint` | FK → `users.id`, not null |
| `seller_id` | `bigint` | FK → `users.id`, not null |
| `currency_id` | `bigint` | FK → `currencies.id`, not null |
| `price` | `numeric(30,8)` | not null |
| `amount` | `numeric(30,8)` | not null |
| `created_at` | `timestamp` | not null, default `now()` |

`buyer_id`, `seller_id` и `currency_id` можно было бы достать JOIN-ом через `orders`. Они хранятся специально, чтобы «Мои сделки» были простым `where buyer_id = ? or seller_id = ?`. На защите это нужно уметь объяснить как осознанную денормализацию.

### 2.7 `favorites`: M2M
| Колонка | Тип | Ограничения |
|---|---|---|
| `user_id` | `bigint` | FK → `users.id`, `on delete cascade` |
| `currency_id` | `bigint` | FK → `currencies.id`, `on delete cascade` |
| | | PK (`user_id`, `currency_id`) |

Связь **M2M** `users` ↔ `currencies`: «избранные монеты», звёздочка на странице рынка.

### 2.8 `02_seed.sql`
- Монеты: `USDT` (`is_quote = true`), `BTC`, `ETH`, `TON`.
- Пользователей в seed нет: они создаются через регистрацию, потому что пароль хэшируется BCrypt.

### Итог по связям
- O2M: `users`→`recovery_words`, `users`→`wallets`, `currencies`→`wallets`, `users`→`orders`, `currencies`→`orders`, `orders`→`trades` (дважды).
- M2M: `users`↔`currencies` через `favorites`.
- 3+ FK: `trades` (5 FK).

---

## 3. Структура файлов

```
Tradance/
├── pom.xml
├── TZ.md
├── sql/
│   ├── 01_schema.sql
│   └── 02_seed.sql
└── src/main/
    ├── java/ru/itis/
    │   ├── models/
    │   │   ├── User.java, Role.java (enum)
    │   │   ├── RecoveryWord.java
    │   │   ├── Currency.java
    │   │   ├── Wallet.java
    │   │   ├── Order.java, OrderSide.java (enum), OrderStatus.java (enum)
    │   │   └── Trade.java
    │   ├── dto/
    │   │   ├── SignUpForm.java, LoginForm.java, PhraseCheckForm.java, RecoverForm.java
    │   │   ├── CurrencyForm.java, OrderForm.java
    │   │   ├── SessionUser.java            ← что лежит в сессии после входа
    │   │   ├── MarketRow.java              ← монета + последняя цена + избранное
    │   │   ├── OrderBookDto.java, OrderBookLevel.java
    │   │   └── ApiResponse.java            ← ответ AJAX: ok / error / data
    │   ├── exceptions/
    │   │   ├── ValidationException.java    ← Map<String, String> errors (поле → текст)
    │   │   └── BusinessException.java      ← «недостаточно средств», «заблокирован» и т.п.
    │   ├── repository/
    │   │   ├── CrudRepository.java
    │   │   ├── UsersRepository.java            + UsersRepositoryJdbcImpl.java
    │   │   ├── RecoveryWordsRepository.java    + RecoveryWordsRepositoryJdbcImpl.java
    │   │   ├── CurrenciesRepository.java       + CurrenciesRepositoryJdbcImpl.java
    │   │   ├── WalletsRepository.java          + WalletsRepositoryJdbcImpl.java
    │   │   ├── OrdersRepository.java           + OrdersRepositoryJdbcImpl.java
    │   │   └── TradesRepository.java           + TradesRepositoryJdbcImpl.java
    │   ├── services/
    │   │   ├── AuthService.java            + AuthServiceImpl.java
    │   │   ├── RecoveryPhraseService.java  + RecoveryPhraseServiceImpl.java
    │   │   ├── UsersService.java           + UsersServiceImpl.java
    │   │   ├── CurrenciesService.java      + CurrenciesServiceImpl.java
    │   │   ├── WalletsService.java         + WalletsServiceImpl.java
    │   │   ├── OrdersService.java          + OrdersServiceImpl.java
    │   │   ├── MatchingEngine.java         ← алгоритм сведения, вызывается из OrdersServiceImpl
    │   │   └── FileStorageService.java     + FileStorageServiceImpl.java
    │   ├── listeners/
    │   │   └── AppContextListener.java
    │   ├── filters/
    │   │   ├── AuthFilter.java
    │   │   └── AdminFilter.java
    │   └── servlets/
    │       ├── MarketServlet.java          /market
    │       ├── TradeServlet.java           /trade
    │       ├── SignUpServlet.java          /signup
    │       ├── PhraseServlet.java          /signup/phrase
    │       ├── LoginServlet.java           /login
    │       ├── LoginVerifyServlet.java     /login/verify
    │       ├── LogoutServlet.java          /logout
    │       ├── RecoverServlet.java         /recover, /recover/verify
    │       ├── WalletsServlet.java         /wallets
    │       ├── MyOrdersServlet.java        /orders
    │       ├── MyTradesServlet.java        /trades
    │       ├── ProfileServlet.java         /profile
    │       ├── FileServlet.java            /files/*
    │       ├── api/
    │       │   ├── OrderBookApiServlet.java    /api/orderbook
    │       │   ├── OrdersApiServlet.java       /api/orders, /api/orders/cancel
    │       │   └── FavoritesApiServlet.java    /api/favorites
    │       └── admin/
    │           ├── AdminCurrenciesServlet.java /admin/currencies (+ /new, /edit, /delete)
    │           ├── AdminUsersServlet.java      /admin/users (+ /block, /deposit)
    │           └── AdminOrdersServlet.java     /admin/orders
    ├── resources/
    │   ├── db.properties
    │   ├── app.properties                  ← upload.dir, start.balance
    │   └── wordlist.txt                    ← словарь BIP39 (2048 англ. слов)
    └── webapp/
        ├── index.jsp                       ← только <c:redirect url="/market"/>
        ├── WEB-INF/
        │   ├── web.xml
        │   ├── tags/
        │   │   ├── layout.tag              ← каркас страницы: head + navbar + doBody + footer
        │   │   ├── navbar.tag              ← меню зависит от роли
        │   │   ├── footer.tag
        │   │   ├── fieldError.tag          ← вывод ошибки под полем
        │   │   ├── flash.tag               ← одноразовое сообщение из сессии
        │   │   ├── money.tag               ← формат числа (fmt:formatNumber)
        │   │   └── statusBadge.tag         ← цветной статус заявки
        │   └── jsp/
        │       ├── market.jsp, trade.jsp
        │       ├── signup.jsp, phrase.jsp, login.jsp, login_verify.jsp
        │       ├── recover.jsp, recover_verify.jsp
        │       ├── wallets.jsp, orders.jsp, trades.jsp, profile.jsp
        │       ├── error.jsp
        │       └── admin/
        │           ├── currencies.jsp, currency_form.jsp
        │           ├── users.jsp
        │           └── orders.jsp
        ├── css/style.css
        └── js/
            ├── trade.js                    ← опрос стакана, выставление и отмена заявки
            ├── favorites.js                ← звёздочка
            └── forms.js                    ← блокировка кнопки после submit, проверка совпадения паролей
```

JSP лежат в `WEB-INF/jsp`, чтобы их нельзя было открыть напрямую по URL, минуя сервлет.

---

## 4. `pom.xml`

Взять из `HwOct24/pom.xml`: `javax.servlet-api 4.0.1` (provided), `jstl 1.2`, `postgresql`, `spring-jdbc` (только ради `DriverManagerDataSource`), `lombok` (provided), а также `maven-compiler-plugin` с `annotationProcessorPaths`.

Добавить:

| Зависимость | Зачем |
|---|---|
| `com.fasterxml.jackson.core:jackson-databind` | JSON для AJAX (версию взять из ClassWork) |
| `org.springframework.security:spring-security-crypto` | `BCryptPasswordEncoder`. Весь `spring-security-web` не нужен |

**JdbcTemplate не использовать нигде.** Сам `spring-jdbc` остаётся только ради `DriverManagerDataSource`; можно ли так, уточнить у препода.

---

## 5. Контракты: модели и DTO

### Модели (Lombok: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- `User`: `Long id`, `String username`, `String email`, `String passwordHash`, `Role role`, `boolean blocked`, `String avatarPath`, `LocalDateTime createdAt`.
- `RecoveryWord`: `Long id`, `Long userId`, `int position`, `String wordHash`.
- `Currency`: `Long id`, `String ticker`, `String name`, `String iconPath`, `boolean quote`, `boolean active`.
- `Wallet`: `Long id`, `Long userId`, `Long currencyId`, `BigDecimal balance`, `BigDecimal locked`. Метод `getAvailable()` возвращает `balance − locked`.
- `Order`: `Long id`, `Long userId`, `Long currencyId`, `OrderSide side`, `BigDecimal price`, `BigDecimal amount`, `BigDecimal filled`, `OrderStatus status`, `LocalDateTime createdAt`. Метод `getRemaining()` возвращает `amount − filled`.
- `Trade`: все колонки из 2.6.
- Enum-ы: `Role { USER, ADMIN }`, `OrderSide { BUY, SELL }`, `OrderStatus { OPEN, PARTIAL, FILLED, CANCELLED }`.

### Правила `BigDecimal`
- Сравнивать через `compareTo`, **не** через `equals`: у `2.0` и `2.00` `equals` возвращает `false`.
- Создавать из строки (`new BigDecimal("0.1")`), не из `double`.
- Масштаб 8 знаков. Округление при умножении `price × amount`: `setScale(8, RoundingMode.UP)` для блокировки при BUY, чтобы заблокировать не меньше, чем нужно.
- В JDBC использовать `setBigDecimal` и `getBigDecimal`.

### DTO
- Формы (`SignUpForm` и др.) хранят все поля типа `String`, ровно как пришли из `request`. Парсинг и проверку делает **сервис**.
- `SessionUser`: `Long id`, `String username`, `Role role`, `String avatarPath`. В сессию кладётся он, а не `User`, чтобы хэш пароля не лежал в сессии.
- `OrderBookLevel`: `BigDecimal price`, `BigDecimal amount` (сумма остатков на этой цене).
- `OrderBookDto`: `List<OrderBookLevel> bids` (BUY, цена по убыванию), `List<OrderBookLevel> asks` (SELL, цена по возрастанию), `BigDecimal lastPrice`, `List<Trade> lastTrades` (20 последних).
- `ApiResponse`: `boolean ok`, `String error`, `Map<String, String> fieldErrors`, `Object data`.

---

## 6. Контракты: репозитории

### `CrudRepository<T>`
```java
Optional<T> findById(Long id);
List<T> findAll();
void save(T entity);      // после insert проставляет entity.setId(...) — через "insert ... returning id"
void update(T entity);
void delete(Long id);
```

### Специфичные методы
| Интерфейс | Методы сверх CRUD |
|---|---|
| `UsersRepository` | `findByUsername(String)`, `findByEmail(String)`, `existsByUsername(String)`, `existsByEmail(String)`, `updateBlocked(Long id, boolean)`, `updateAvatar(Long id, String path)`, `updatePasswordHash(Long id, String hash)` |
| `RecoveryWordsRepository` | `saveAll(Long userId, List<String> hashes)` (позиции 1..10 по порядку), `findByUserAndPositions(Long userId, List<Integer> positions)` |
| `CurrenciesRepository` | `findByTicker(String)`, `findAllActiveTradable()` (active и не quote), `findQuote()`, `isUsed(Long id)` (есть ли кошельки или заявки), `findFavoriteIds(Long userId)`, `addFavorite(Long userId, Long currencyId)`, `removeFavorite(...)`, `isFavorite(...)` |
| `WalletsRepository` | `findAllByUser(Long userId)`, `findByUserAndCurrency(Long userId, Long currencyId)`, транзакционные методы (см. 6.1) |
| `OrdersRepository` | `findAllByUser(Long userId)`, `findOrderBook(Long currencyId)` (группировка по цене), транзакционные методы (см. 6.1) |
| `TradesRepository` | `findLastByCurrency(Long currencyId, int limit)`, `findLastPrice(Long currencyId)`, `findAllByUser(Long userId)`, транзакционный `save(Connection, Trade)` |

### 6.1 Транзакции: как провести одну транзакцию через несколько репозиториев
Обычно каждый метод репозитория берёт своё соединение из `DataSource`. Транзакция же должна идти **в одном `Connection`**. Решение:
- у методов, которые участвуют в сделке, есть перегрузка с первым параметром `Connection con`, и такой метод соединение **не закрывает**;
- соединение открывает и закрывает сервис (`OrdersServiceImpl`): `setAutoCommit(false)`, затем все шаги, затем `commit()`, а при любом исключении `rollback()`.

Методы с `Connection`:
- `WalletsRepository`:
  - `lockFunds(con, walletId, amount)`: одним `UPDATE` с условием `balance - locked >= ?`. Метод возвращает `true`, если обновлена 1 строка, и `false`, если средств мало.
  - `unlock(con, walletId, amount)`
  - `spendLocked(con, walletId, spend, unlock)`: `balance -= spend`, `locked -= unlock`.
  - `credit(con, walletId, amount)`
  - `findOrCreate(con, userId, currencyId)`
- `OrdersRepository`:
  - `save(con, order)`
  - `findMatching(con, currencyId, side, price, excludeUserId)`: встречные заявки в порядке приоритета.
  - `updateFill(con, orderId, filled, status)`
  - `findById(con, id)`
- `TradesRepository`: `save(con, trade)`.

Общие правила для всех `JdbcImpl`:
- SQL держать в `private static final String`.
- Маппинг строки результата вынести в один приватный `mapRow(ResultSet)`.
- `SQLException` заворачивать в `IllegalStateException`.
- Склейки строк в SQL нет нигде, только `?`.

---

## 7. Контракты: сервисы

### `AuthService`
```java
List<String> signUp(SignUpForm form);              // создаёт user + кошелёк USDT + фразу; возвращает 10 слов (открытым текстом, один раз)
List<Integer> startLogin(LoginForm form);          // проверка пароля; возвращает 3 случайные позиции
SessionUser finishLogin(Long userId, List<Integer> positions, PhraseCheckForm form);
List<Integer> startRecover(String username);       // 3 позиции для восстановления
void finishRecover(String username, List<Integer> positions, RecoverForm form); // слова + новый пароль
```
Проверки `signUp` (ошибки собираются в `ValidationException`, а не в первую попавшуюся):
- все поля заполнены (после `trim`);
- `username`: 3–30 символов, только `a-z`, `A-Z`, `0-9`, `_`;
- `email` похож на email;
- пароль: ≥ 8 символов, есть буква и цифра;
- `password` совпадает с `passwordRepeat`;
- `username` и `email` ещё не заняты.

Проверки `startLogin`:
- пользователь существует и пароль совпадает. При ошибке сообщение одинаковое («неверный логин или пароль»), чтобы нельзя было узнать, какие логины есть;
- пользователь не заблокирован.

### `RecoveryPhraseService`
```java
List<String> generate();                           // 10 разных слов из словаря через SecureRandom
List<String> hashAll(List<String> words);          // каждое слово отдельно: trim + toLowerCase + BCrypt
List<Integer> pickPositions();                     // 3 разные случайные позиции из 1..10, по возрастанию
boolean check(Long userId, List<Integer> positions, List<String> answers);
```
Словарь загружается **один раз** в listener из `wordlist.txt` и передаётся в конструктор.

### `UsersService`
`findAll()`, `block(Long id)`, `unblock(Long id)`, `updateAvatar(Long userId, Part file)`.
Админ не может заблокировать сам себя.

### `CurrenciesService`
- `getMarket(Long userIdOrNull)`: `List<MarketRow>` (монета, последняя цена, в избранном ли).
- CRUD: `create(CurrencyForm, Part icon)`, `update(Long id, CurrencyForm, Part icon)`, `delete(Long id)`, `findById`, `findAll`.
- `toggleFavorite(Long userId, Long currencyId)`: возвращает новое состояние.

Проверки:
- `ticker`: 2–10 заглавных латинских букв, уникальный;
- `name` не пустое;
- `USDT` нельзя удалить или деактивировать;
- `delete`: если монета используется (`isUsed`), бросить `BusinessException("Монета используется — можно только деактивировать")`.

### `WalletsService`
- `getWallets(Long userId)`: кошельки с тикером, балансом, заблокированным и доступным.
- `deposit(Long userId, Long currencyId, String amount)`: только для админа, тестовое пополнение, сумма > 0.

### `OrdersService`
```java
Order place(Long userId, OrderForm form);          // валидация + блокировка средств + матчинг, всё в одной транзакции
void cancel(Long userId, Long orderId);
List<Order> getMyOrders(Long userId);
List<Trade> getMyTrades(Long userId);
OrderBookDto getOrderBook(String ticker);
```
Проверки `place`:
- монета существует, активна и не `USDT`;
- `side` равен `BUY` или `SELL`;
- `price` и `amount` являются числами, > 0, не больше 8 знаков после точки;
- пользователь не заблокирован.

Проверки `cancel`:
- заявка принадлежит этому пользователю;
- статус `OPEN` или `PARTIAL`. Иначе бросить `BusinessException`.

### `MatchingEngine`: алгоритм (реализуешь сам)
Метод вида `void match(Connection con, Order incoming)`.

1. Найти встречные заявки (`findMatching`):
   - для `BUY`: `SELL` той же монеты, статус `OPEN`/`PARTIAL`, `price <= incoming.price`, не свои; сортировка по цене ↑, затем по `created_at` ↑;
   - для `SELL`: `BUY`, `price >= incoming.price`, не свои; сортировка по цене ↓, затем по `created_at` ↑.
2. Для каждой встречной, пока у `incoming` есть остаток:
   - `q = min(остаток incoming, остаток встречной)`;
   - `p = цена встречной` (она стояла в стакане раньше);
   - провести расчёт по формулам ниже;
   - обновить `filled` и статусы обеих заявок: `FILLED`, если остаток 0, иначе `PARTIAL`;
   - сохранить `Trade`.
3. Если после цикла остаток > 0, заявка остаётся в стакане.

Формулы расчёта для покупателя `B` (его лимит `pB`) и продавца `S`:

| Кошелёк | Изменение |
|---|---|
| USDT покупателя | `balance −= p·q`, `locked −= pB·q` (при `p < pB` разница сама становится доступной) |
| монета покупателя | `balance += q` (`findOrCreate`) |
| монета продавца | `balance −= q`, `locked −= q` |
| USDT продавца | `balance += p·q` |

Блокировка при выставлении заявки:
- `BUY`: заблокировать `price × amount` USDT;
- `SELL`: заблокировать `amount` монет.

Если `lockFunds` вернул `false`, бросить `BusinessException("Недостаточно средств")`.

Отмена заявки разблокирует остаток:
- `BUY`: `price × remaining` USDT;
- `SELL`: `remaining` монет.

**Одновременные запросы.** Метод `place` объявить `synchronized`: на одном Tomcat этого достаточно, чтобы две заявки не матчились одновременно. На защите честно сказать, что в реальной бирже так не делают и используют блокировки строк в БД (`select ... for update`) или очередь.

### `FileStorageService`
- `String save(Part part)`. Проверки:
  - файл не пустой;
  - `contentType` равен `image/png`, `image/jpeg` или `image/webp`;
  - размер ≤ 2 МБ.

  Имя файла генерируется через `UUID` с правильным расширением, потому что имени от пользователя доверять нельзя (`../../`). Файл сохраняется в `upload.dir` из `app.properties`, папка находится **вне** проекта, иначе файлы пропадут при redeploy.
- `Path load(String fileName)`: проверить, что имя не содержит `/` и `..`.

---

## 8. Listener, фильтры, web.xml

### `AppContextListener` (`@WebListener`)
1. Прочитать `db.properties` и `app.properties` из classpath.
2. Создать `DriverManagerDataSource`.
3. Создать все `...JdbcImpl`, затем `BCryptPasswordEncoder`, загрузить словарь `wordlist.txt` в `List<String>` и проверить, что в нём 2048 слов.
4. Создать сервисы, передавая им **интерфейсы** через конструктор.
5. Положить в `ServletContext` каждый сервис под константой: `AUTH_SERVICE`, `USERS_SERVICE`, `CURRENCIES_SERVICE`, `WALLETS_SERVICE`, `ORDERS_SERVICE`, `FILE_STORAGE_SERVICE`, а также `OBJECT_MAPPER` (Jackson).

### Атрибуты сессии (тоже константы)
| Ключ | Что лежит | Когда |
|---|---|---|
| `user` | `SessionUser` | после **второго** шага входа |
| `pendingUserId` | `Long` | после верного пароля, до проверки слов |
| `challengePositions` | `List<Integer>` | 3 позиции для проверки |
| `challengeAttempts` | `Integer` | неудачные попытки ввода слов |
| `phraseToShow` | `List<String>` | 10 слов после регистрации, удаляется сразу после показа |
| `flash` | `String` | одноразовое сообщение после redirect, удаляется после показа |

### `AuthFilter` (`/*`)
- Публичные пути пропускает всегда: `/market`, `/trade`, `/signup*`, `/login`, `/recover*`, `/api/orderbook`, `/files/*`, `/css/*`, `/js/*`, корень.
- `/login/verify` пропускает только при наличии `pendingUserId`.
- Остальные пути: если `user` в сессии нет, для `/api/*` отвечает `401` с JSON, иначе делает redirect на `/login`.
- Если пользователь заблокирован (проверка по БД через сервис), делает `invalidate` и redirect на `/login` с flash-сообщением.

### `AdminFilter` (`/admin/*`)
Если `user == null` или его роль не `ADMIN`, отвечает `403` и делает forward на `error.jsp`. Проверка на `null` нужна обязательно: порядок `@WebFilter`-ов не гарантирован. Чтобы задать порядок явно, объявить `<filter-mapping>` в `web.xml`: сначала `AuthFilter`, потом `AdminFilter`.

### `web.xml`
Схема 4.0. Содержит `request-character-encoding` и `response-character-encoding` UTF-8, `<filter-mapping>` для порядка фильтров, `<error-page>` для 404 и 500, ведущие на `/WEB-INF/jsp/error.jsp`.

---

## 9. Сервлеты и страницы

Все формы работают по одной схеме:
- `GET` делает forward на JSP;
- `POST` при успехе: flash-сообщение, затем `sendRedirect` (PRG);
- `POST` при `ValidationException`: `req.setAttribute("errors", ...)` и `req.setAttribute("form", form)`, затем forward на ту же JSP. Введённые значения сохраняются, кроме паролей.

### Регистрация и вход
| URL | GET | POST |
|---|---|---|
| `/signup` | форма: username, email, password, passwordRepeat | `authService.signUp`, слова в `phraseToShow`, redirect на `/signup/phrase` |
| `/signup/phrase` | показать 10 слов с номерами, удалить `phraseToShow` из сессии. Если атрибута нет, redirect на `/login` | — |
| `/login` | форма | `startLogin`, затем `pendingUserId` и `challengePositions` в сессию, redirect на `/login/verify` |
| `/login/verify` | «Введите слова №X, №Y, №Z» | `finishLogin`. Успех: убрать `pending*`, положить `user`, redirect на `/market`. Ошибка: `attempts++`, после 3 ошибок `invalidate` и redirect на `/login` |
| `/logout` | — | `invalidate`, redirect на `/market` (только POST: выход — это изменение состояния) |
| `/recover` | форма username | позиции в сессию, redirect на `/recover/verify` |
| `/recover/verify` | 3 слова + новый пароль ×2 | `finishRecover`, flash «Пароль изменён», redirect на `/login` |

На `phrase.jsp` показать предупреждение: «Запишите слова. Больше они не будут показаны». По желанию добавить чекбокс «Я записал» перед кнопкой «Продолжить».

### Торговля
| URL | Что делает |
|---|---|
| `GET /market` | таблица монет: иконка, тикер, название, последняя цена, ★ (только для вошедших). Клик по строке ведёт на `/trade?currency=BTC` |
| `GET /trade?currency=BTC` | стакан (asks сверху, bids снизу), последние сделки, форма заявки (для вошедших), доступный баланс. Нет монеты: 404 |
| `GET /wallets` | таблица кошельков: валюта, всего, заблокировано, доступно |
| `GET /orders` | мои заявки: статус через `statusBadge.tag`, кнопка «Отменить» у `OPEN`/`PARTIAL` (AJAX) |
| `GET /trades` | мои сделки: покупка или продажа, монета, цена, количество, сумма, время |
| `GET/POST /profile` | данные пользователя и загрузка аватара (`enctype="multipart/form-data"`), PRG |
| `GET /files/{name}` | `fileStorageService.load`, выставить `Content-Type`, скопировать байты в `resp.getOutputStream()`. Нет файла: 404 |

### AJAX (JSON через Jackson, ответ всегда `ApiResponse`)
| URL | Что делает | Что меняется на странице |
|---|---|---|
| `GET /api/orderbook?currency=BTC` | `OrderBookDto` | `trade.js` раз в 3 секунды (`setInterval` + `fetch`) перерисовывает стакан, последние сделки и последнюю цену |
| `POST /api/orders` | `ordersService.place` | без перезагрузки: очистить форму, показать сообщение в блоке над формой (не `alert`), обновить стакан и баланс. Ошибки полей показать под полями |
| `POST /api/orders/cancel` | `ordersService.cancel` | строка заявки меняет статус на `CANCELLED`, кнопка исчезает |
| `POST /api/favorites` | `toggleFavorite` | звёздочка ☆/★ переключается |

Пока запрос в `trade.js` выполняется, кнопка «Купить/Продать» заблокирована (защита от двойного клика). Ответ `401` означает, что нужно показать ссылку «Войдите».

### Админка (`/admin/*`)
| URL | Что делает |
|---|---|
| `GET /admin/currencies` | список монет: «Изменить», «Удалить» (POST-форма с `confirm()`), «Добавить» |
| `GET/POST /admin/currencies/new` | форма с тикером, названием, иконкой (multipart) и флагом активности |
| `GET/POST /admin/currencies/edit?id=` | та же форма, заполненная текущими данными |
| `POST /admin/currencies/delete` | удаление; если монета используется, flash с ошибкой |
| `GET /admin/users` | пользователи: роль, статус, кнопки «Заблокировать/Разблокировать» и форма «Пополнить» (монета + сумма) |
| `GET /admin/orders` | все заявки и все сделки |

Полный CRUD монет закрывает требование ТЗ «полный CRUD хотя бы для одной сущности».

---

## 10. Шаблоны (свои теги)

Каждая страница выглядит так:
```
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<ui:layout title="Рынок"> ...содержимое... </ui:layout>
```
- `layout.tag` (атрибут `title`): `<!DOCTYPE html>`, `<head>` с `style.css`, `<ui:navbar/>`, `<ui:flash/>`, `<main><jsp:doBody/></main>`, `<ui:footer/>`.
- `navbar.tag`:
  - гостю: «Рынок», «Вход», «Регистрация»;
  - пользователю: «Рынок», «Кошельки», «Заявки», «Сделки», аватар, «Выход»;
  - админу: то же плюс «Админка».
- `fieldError.tag` (атрибут `field`): `<c:if>` с ошибкой из `errors[field]`.
- `money.tag` (атрибут `value`): `fmt:formatNumber` до 8 знаков после точки без хвостовых нулей.
- `statusBadge.tag` (атрибут `status`): `<span class="badge badge-...">`.

В JSP **нет** скриптлетов `<% %>`, только EL и JSTL (`c:`, `fmt:`).

---

## 11. Вёрстка

- Свой `style.css`, без Bootstrap: меньше вопросов на защите.
- Раскладка через CSS Grid и Flexbox. Страница пары: две колонки (стакан | форма + сделки), на узком экране одна.
- Цвета: покупка зелёная, продажа красная. Числа выровнены вправо, шрифт с цифрами одной ширины (`font-variant-numeric: tabular-nums`).
- У каждого поля формы есть `<label>`. HTML5-валидация (`required`, `type="email"`, `min`, `step="0.00000001"`) — это удобство для пользователя, а настоящая проверка всё равно на сервере.

---

## 12. Поток данных

```
Регистрация
POST /signup ─▶ SignUpServlet ─▶ AuthService.signUp
                                   ├▶ проверки ─(ошибки)─▶ forward signup.jsp + errors
                                   ├▶ UsersRepository.save (BCrypt пароля)
                                   ├▶ WalletsRepository: USDT = 10 000
                                   └▶ RecoveryPhraseService.generate → hashAll → RecoveryWordsRepository.saveAll
             ◀─ 302 /signup/phrase ─ (10 слов в сессии, показываются один раз)

Вход (2 шага)
POST /login ─▶ startLogin (пароль ок, не заблокирован) ─▶ session: pendingUserId, позиции [2, 5, 9]
            ◀─ 302 /login/verify
POST /login/verify ─▶ finishLogin ─▶ BCrypt.matches по каждому слову
            ◀─ 302 /market (session: user)          или ошибка, attempts++

Заявка (AJAX)
fetch POST /api/orders ─▶ AuthFilter ─▶ OrdersApiServlet ─▶ OrdersService.place  [synchronized]
      con.setAutoCommit(false)
        ├▶ lockFunds ─(false)─▶ BusinessException «Недостаточно средств»
        ├▶ orders.save(con, order)
        ├▶ MatchingEngine.match(con, order)
        │     цикл: findMatching → расчёт по кошелькам → updateFill × 2 → trades.save
        └▶ commit()      (при исключении rollback())
      ◀─ JSON ApiResponse ─▶ trade.js перерисовывает стакан и баланс
```

---

## 13. Порядок работы

1. Спросить у препода про тему, транзакции и `spring-jdbc`/`DriverManagerDataSource`.
2. Написать `01_schema.sql` и `02_seed.sql`, выполнить их, проверить связи в DataGrip (диаграмма).
3. Создать проект: `pom.xml`, `web.xml`, `db.properties`, `app.properties`, `wordlist.txt`.
4. Написать `models`, `dto`, `exceptions`.
5. Написать репозитории: сначала интерфейсы, потом `JdbcImpl`. Проверить каждый простым вызовом из listener или `main`.
6. Написать `layout.tag`, `navbar.tag` и пустые страницы, чтобы каркас был готов сразу.
7. Регистрация, затем показ фразы, затем вход в 2 шага, затем выход, затем восстановление.
8. `AuthFilter`, `AdminFilter`.
9. Админка: CRUD монет (с иконками, то есть `FileStorageService` и `FileServlet`), пользователи, пополнение.
10. Рынок, страница пары, кошельки.
11. Заявки и `MatchingEngine` с транзакцией, затем отмена.
12. AJAX: стакан, заявки, избранное.
13. Профиль и аватар.
14. Вёрстка, пройти чек-лист.
15. Бонус (по желанию): хэш-цепочка сделок, раздел 16.

---

## 14. Чек-лист приёмки

**БД и сущности (3 балла)**
- [ ] 6 таблиц + `favorites`; есть O2M, есть M2M; `trades` имеет 5 FK.
- [ ] Деньги в `NUMERIC`, в Java в `BigDecimal`.
- [ ] Отдельный интерфейс репозитория на каждую модель, только `PreparedStatement`, без JdbcTemplate и ORM.

**Архитектура (2 балла)**
- [ ] Сервлет → сервис (интерфейс) → репозиторий (интерфейс). В сервлетах нет SQL и JDBC.
- [ ] Всё создаётся в listener, атрибуты читаются по константам.
- [ ] В JSP нет Java-кода.

**Проверки и бизнес-логика (5 баллов)**
- [ ] Регистрация: пустые поля, формат, длина пароля, совпадение паролей, занятый логин или email. Ошибки выводятся под полями, введённое сохраняется.
- [ ] Вход: неверный пароль даёт общую ошибку; заблокированного не пускает; без проверки 3 слов в `/wallets` не попасть.
- [ ] 3 неверные попытки ввода слов возвращают на `/login`.
- [ ] Фраза показывается ровно один раз (после F5 её уже нет).
- [ ] Заявка: недостаточно средств даёт ошибку и ничего не блокирует.
- [ ] Две встречные заявки по пересекающейся цене исполняются, балансы обоих пользователей сходятся до копейки.
- [ ] Частичное исполнение: статус `PARTIAL`, остаток виден в стакане.
- [ ] Покупка по цене ниже своего лимита: излишек разблокирован.
- [ ] Отмена размораживает остаток. Чужую заявку отменить нельзя (попробовать подменить `id` в запросе).
- [ ] Если вручную вызвать исключение посреди матчинга, `rollback` срабатывает и балансы не меняются.
- [ ] Удаление используемой монеты не проходит, вместо этого выводится понятное сообщение.

**Общие правила (4 балла)**
- [ ] После каждого успешного POST происходит redirect, F5 ничего не дублирует.
- [ ] Кнопки блокируются после отправки.
- [ ] Гость на `/wallets` попадает на `/login`; `USER` на `/admin/*` получает 403.
- [ ] Кириллица сохраняется корректно.
- [ ] AJAX меняет страницу без `alert`.
- [ ] Загрузка и показ файлов через `FileServlet`; нельзя загрузить `.exe` или файл больше 2 МБ.

**Вёрстка (1 балл)**
- [ ] Единый layout, понятная навигация, адаптивность (страница пары на узком экране).

---

## 15. Вопросы, которые могут задать на защите

- Чем `forward` отличается от `sendRedirect`? Что такое PRG?
- Как работает фильтр и `chain.doFilter`? Почему в `AdminFilter` есть проверка на `null`?
- Зачем `ServletContextListener`? Почему сервлет зависит от интерфейса сервиса?
- Почему `PreparedStatement`? Что такое SQL-инъекция?
- Почему деньги в `BigDecimal`, а не в `double`? Почему `compareTo`, а не `equals`?
- Что будет, если упадёт середина сделки? Как работают `commit` и `rollback`? Зачем один `Connection` на всю транзакцию?
- Как работает матчинг? По какой цене проходит сделка и почему?
- Что будет, если две заявки придут одновременно? Зачем `synchronized` и чем это плохо в реальной системе?
- Зачем `locked` в кошельке?
- Почему каждое слово фразы хэшируется отдельно? Почему позиции лежат в сессии, а не в hidden-поле? Это настоящая 2FA? (Нет: это вторая проверка знанием.)
- Почему `SecureRandom`, а не `Random`?
- Зачем BCrypt и что такое соль?
- Почему загруженный файл переименовывается в UUID? Почему папка загрузок вне проекта?
- Почему в сессии лежит `SessionUser`, а не `User`?
- Где в проекте O2M, а где M2M? Почему `trades` хранит `buyer_id`, если его можно получить JOIN-ом?
- Что такое `<jsp:doBody/>` и чем `.tag` отличается от `<jsp:include>`?
- Как работает `fetch`? Почему AJAX-ответ в JSON? Что делает Jackson?

---

## 16. Бонус: хэш-цепочка сделок (после основного)

- Таблица `blocks`: `index`, `created_at`, `prev_hash`, `hash`, `nonce`, плюс связь со сделками (FK `block_id` в `trades`, null пока сделка не «запечатана»).
- `hash = SHA-256(index + prev_hash + created_at + id и данные сделок + nonce)` через `MessageDigest`.
- Каждые N сделок (или по кнопке в админке) незапечатанные сделки собираются в новый блок.
- Страница `/explorer`: список блоков и кнопка «Проверить целостность». Сервис пересчитывает все хэши и находит первый блок, где цепочка сломана (например, если в БД руками изменили сделку).
