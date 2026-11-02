
const rows = document.querySelectorAll('.book-row');


for(let i = 0; i < rows.length; i++){
    rows[i].addEventListener('click', () =>
    {
        let price = rows[i].dataset.price;
        let side = rows[i].dataset.side;

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



    });
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