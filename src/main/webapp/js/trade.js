
const book = document.getElementById('book');




if(book != null){
    book.addEventListener('click', (e) => {
        const row = e.target.closest('.book-row');

        if(row == null) return;
        let price = row.dataset.price;
        let side = row.dataset.side;

        price = String(Number(price));

        let priceInput;
        let amountInput;


        if(side == 'SELL'){
            priceInput = document.getElementById('buy-price');
            amountInput = document.getElementById('buy-amount');
        }else {
            priceInput = document.getElementById('sell-price');
            amountInput = document.getElementById('sell-amount');
        }

        if (priceInput == null) {
            return;
        }

        priceInput.value = price;
        updateTotal(side == 'SELL' ? 'buy': 'sell')
        amountInput.focus();
    })
}

const updateTotal = (side) => {
    const priceElement = document.querySelector('#' + side + '-price');
    const amountElement = document.querySelector('#' + side + '-amount');

    const inputPrice = priceElement ? Number(priceElement.value) : 0;
    const inputAmount = amountElement ? Number(amountElement.value) : 0;

    let total = inputPrice * inputAmount;
    const spanTotal = document.querySelector('#'+side+'-total');
    if(spanTotal == null) return;
    spanTotal.textContent = (isNaN(total)) ? '0' : total.toFixed(2);

}

const fmt = (x) => {
    return Number(x).toLocaleString('ru-RU', {maximumFractionDigits: 8})
}

const renderLevels = (tbody, levels, side) => {
    if(tbody == null) return;
    const cls = side == 'SELL' ? 'sell' : 'buy';
    let html = '';
    for(let i = 0; i < levels.length; i++){
        let lvl = levels[i];
        const sum = Number(lvl.price) * Number(lvl.amount);
        html += '<tr class="book-row" data-price="' + lvl.price + '" data-side="' + side + '">';
        html += '<td class="' + cls + '">' + fmt(lvl.price) + '</td>';
       html += '<td class="num">' + fmt(lvl.amount) + '</td>';
       html += '<td class="num muted">' + sum.toLocaleString('ru-RU', {maximumFractionDigits: 2}) + '</td>';
       html += '</tr>';
    }
    tbody.innerHTML = html;
}



const buyElement = document.querySelector('#buy-price');
const buyAmount = document.querySelector('#buy-amount');

if(buyElement != null) {
    buyElement.addEventListener('input', () => {
        updateTotal('buy');

    })
    buyAmount.addEventListener('input', () => {
        updateTotal('buy');
    })

}


const sellElement = document.querySelector('#sell-price');
const sellAmount = document.querySelector('#sell-amount');

if(sellAmount != null) {

    sellElement.addEventListener('input', () => {
        updateTotal('sell');
    })
    sellAmount.addEventListener('input', () => {
        updateTotal('sell');
    })
}


updateTotal('buy');
updateTotal('sell');



const refreshBook = async () => {
    const url = book.dataset.ctx + '/api/orderbook?currency=' + book.dataset.ticker;
    const response = await fetch(url);
    const json = await response.json();

    if (!json.ok) return;
    renderLevels(document.getElementById('asks'), json.data.asks, 'SELL');
    renderLevels(document.getElementById('bids'), json.data.bids, 'BUY');

    const mid = document.getElementById('mid-price');
    mid.textContent = json.data.lastPrice ? fmt(json.data.lastPrice) : '—';
}

if (book != null) {
    setInterval(refreshBook, 3000);
}
