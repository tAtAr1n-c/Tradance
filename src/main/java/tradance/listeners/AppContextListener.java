package tradance.listeners;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import tradance.models.User;
import tradance.repository.*;
import tradance.services.*;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

@WebListener
public class AppContextListener implements ServletContextListener {


    @Override
    public void contextInitialized(ServletContextEvent sce) {


        Properties appProp = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("app.properties")) {
            if (in == null) {
                throw new IllegalStateException("app.properties не найден в classpath");
            }
            appProp.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать app.properties", e);
        }


        Properties prop = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("db.properties не найдем в classPath");
            }
            prop.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать настройки приложения", e);
        }


        List<String> wordlist;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("wordlist.txt")) {
            if (in == null) throw new IllegalStateException("wordlist.txt не найден в classpath");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            wordlist = reader.lines().map(String::trim).filter(word -> !word.isEmpty()).toList();
        } catch (IOException e) {
            throw new IllegalStateException("Не получилось прочитать слова из файла для генерации", e);
        }

        String url = prop.getProperty("db.url");
        String username = prop.getProperty("db.username");
        String password = prop.getProperty("db.password");
        String driver = prop.getProperty("db.driver");

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setPassword(password);
        dataSource.setUsername(username);
        dataSource.setDriverClassName(driver);


        CurrenciesRepository currenciesRepository = new CurrenciesRepositoryJdbcImpl(dataSource);
        OrdersRepository ordersRepository = new OrdersRepositoryJdbcImpl(dataSource);
        RecoveryWordsRepository recoveryWordsRepository = new RecoveryWordsRepositoryJdbcImpl(dataSource);
        TradesRepository tradesRepository = new TradesRepositoryJdbcImpl(dataSource);
        UsersRepository usersRepository = new UsersRepositoryJdbcImpl(dataSource);
        WalletsRepository walletsRepository = new WalletsRepositoryJdbcImpl(dataSource);


        String uploadDir = appProp.getProperty("upload.dir");
        String feeRate = appProp.getProperty("fee.rate");
        String feeAccount = appProp.getProperty("fee.account");

        User user = usersRepository.findByUsername(feeAccount).orElseThrow(() -> new IllegalStateException("Счёт комиссий не найден: " + feeAccount));


        RecoveryPhraseService recoveryPhraseService = new RecoveryPhraseServiceImpl(recoveryWordsRepository, wordlist);
        AuthService authService = new AuthServiceImpl(dataSource, usersRepository, walletsRepository, currenciesRepository, recoveryPhraseService);
        UsersService usersService = new UsersServiceImpl(dataSource,usersRepository, walletsRepository, currenciesRepository);
        WalletService walletService = new WalletServiceImpl(dataSource, walletsRepository, currenciesRepository);
        FileStorageService fileStorageService = new FileStorageServiceImpl(uploadDir);
        CurrenciesService currenciesService = new CurrenciesServiceImpl(currenciesRepository, tradesRepository, fileStorageService);
        MatchingEngine matchingEngine = new MatchingEngineImpl(ordersRepository, walletsRepository, tradesRepository,currenciesRepository , new BigDecimal(feeRate) , user.getId());
        OrdersService ordersService = new OrdersServiceImpl(dataSource, ordersRepository, currenciesRepository, tradesRepository, walletsRepository, matchingEngine);


        sce.getServletContext().setAttribute("authService", authService);
        sce.getServletContext().setAttribute("usersService", usersService);
        sce.getServletContext().setAttribute("walletService", walletService);
        sce.getServletContext().setAttribute("fileStorageService", fileStorageService);
        sce.getServletContext().setAttribute("currenciesService", currenciesService);
        sce.getServletContext().setAttribute("ordersService", ordersService);


    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContextListener.super.contextDestroyed(sce);
    }
}
