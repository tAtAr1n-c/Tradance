const markets = document.querySelector("#markets");

if(markets != null){
    markets.addEventListener('click', async (e) => {
        const star = e.target.closest('.star');
        if(star == null) return;

        const url = markets.dataset.ctx + '/api/favorites';
        const response = await fetch(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({currencyId: Number(star.dataset.id)})
        });
        const json = await response.json();
        if(!json.ok) return;

        const favorite = json.data.favorite;
        star.classList.toggle('star-on', favorite);
        star.textContent = favorite ? '★' : '☆';
    });
}
