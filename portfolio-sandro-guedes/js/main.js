(function () {
  "use strict";

  // Menu mobile
  var toggle = document.querySelector(".nav-toggle");
  var nav = document.getElementById("menu-principal");

  function setMenu(open) {
    toggle.setAttribute("aria-expanded", String(open));
    nav.classList.toggle("is-open", open);
  }

  if (toggle && nav) {
    toggle.addEventListener("click", function () {
      setMenu(toggle.getAttribute("aria-expanded") !== "true");
    });
    nav.addEventListener("click", function (event) {
      if (event.target.closest("a")) setMenu(false);
    });
    document.addEventListener("keydown", function (event) {
      if (event.key === "Escape") setMenu(false);
    });
  }

  // Borda no cabeçalho após rolar
  var header = document.querySelector(".site-header");
  function onScroll() {
    header.classList.toggle("is-scrolled", window.scrollY > 8);
  }
  if (header) {
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
  }

  // Destaca no menu a seção visível
  var links = Array.prototype.slice.call(document.querySelectorAll('.site-nav a[href^="#"]'));
  if ("IntersectionObserver" in window && links.length) {
    var byId = {};
    links.forEach(function (link) { byId[link.getAttribute("href").slice(1)] = link; });

    var observer = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (!entry.isIntersecting) return;
        links.forEach(function (link) { link.classList.remove("is-active"); });
        var active = byId[entry.target.id];
        if (active) active.classList.add("is-active");
      });
    }, { rootMargin: "-45% 0px -50% 0px" });

    Object.keys(byId).forEach(function (id) {
      var section = document.getElementById(id);
      if (section) observer.observe(section);
    });
  }

  // Duração dos cargos atuais, contada como no LinkedIn (mês inicial e atual inclusos)
  function formatDuration(totalMonths) {
    var years = Math.floor(totalMonths / 12);
    var months = totalMonths % 12;
    var parts = [];
    if (years) parts.push(years + (years === 1 ? " ano" : " anos"));
    if (months) parts.push(months + (months === 1 ? " mês" : " meses"));
    return parts.join(" e ");
  }

  var now = new Date();
  document.querySelectorAll("[data-since]").forEach(function (el) {
    var parts = el.getAttribute("data-since").split("-");
    var months = (now.getFullYear() - Number(parts[0])) * 12 + (now.getMonth() + 1 - Number(parts[1])) + 1;
    if (months > 0) el.textContent = el.textContent + " · " + formatDuration(months);
  });

  // Ano no rodapé
  var year = document.getElementById("ano-atual");
  if (year) year.textContent = String(now.getFullYear());
})();
