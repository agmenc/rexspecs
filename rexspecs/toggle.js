window.onload = function () {
    const url = window.location.href;
    const title = document.querySelector("h1.title");
    const original = title.innerHTML;

    if (url.includes("/specs/")) {
        title.innerHTML = `<a href="${url.replace("/specs/", "/results/")}">${original}</a>`;
    } else if (url.includes("/results/")) {
        title.innerHTML = `<a href="${url.replace("/results/", "/specs/")}">${original}</a>`;
    }
};