document.addEventListener('submit', (e) => {
    const button = e.target.querySelector('button');
    if(button != null) button.disabled = true
});