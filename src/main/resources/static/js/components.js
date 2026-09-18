// Carregar componentes quando a página HTML estiver pronta
document.addEventListener("DOMContentLoaded", function() {
    const footerEl = document.getElementById("footer-placeholder");
    const contextPath = window.location.pathname.startsWith("/api/") ? "/api" : "";
    
    // Detecta se a página atual está dentro da pasta 'html'
    const appFooter = `
    <!-- Rodapé -->
    <footer class="footer-section py-5 bg-dark-blue text-white">
        <div class="container">
            <div class="row align-items-center text-center text-md-start g-4">
                <div class="col-md-4 text-center">
                    <img src="${contextPath}/images/logo.png" alt="Logo Colmeia Girassol" class="img-fluid" style="max-width: 220px; height: auto; filter: drop-shadow(0 5px 10px rgba(0,0,0,0.1));">
                </div>
                <div class="col-md-4 text-center">
                    <h5 class="fw-bold mb-3 text-white">Fale Conosco</h5>
                    <div class="d-flex align-items-center justify-content-center mb-2">
                        <i class="fa-solid fa-phone fs-5 me-2"></i>
                        <span class="fs-5 fw-bold">(17) 99273-2872</span>
                    </div>
                    <a href="https://wa.me/5517992732872" target="_blank" class="d-flex align-items-center justify-content-center text-decoration-none text-white mt-1">
                        <i class="fa-brands fa-whatsapp fs-4 me-2" style="color: #25D366;"></i>
                        <span class="fw-bold text-light" style="border-bottom: 1px solid #25D366; transition: color 0.3s;" onmouseover="this.style.color='#25D366'" onmouseout="this.style.color=''">Chamar no WhatsApp</span>
                    </a>
                </div>
                <div class="col-md-4 text-center text-md-end">
                    <h5 class="fw-bold mb-3 text-white">Redes Sociais</h5>
                    <div class="social-icons">
                        <a href="https://www.instagram.com/escolainf.colmeia.de_girassol" target="_blank" rel="noopener noreferrer" class="text-white me-3 fs-4"><i class="fa-brands fa-instagram"></i></a>
                    </div>
                </div>
            </div>
            <hr class="mt-4 border-light opacity-25">
            <div class="text-center mt-3">
                <p class="small mb-0 fw-bold opacity-75">&copy; 2026 Escola Infantil Colmeia Girassol. Todos os direitos reservados.</p>
            </div>
        </div>
    </footer>

    <!-- Botão Flutuante WhatsApp -->
    <a href="https://wa.me/5517992732872?text=Olá!%20Gostaria%20de%20saber%20mais%20sobre%20a%20escola." target="_blank" class="btn-whatsapp-float">
        <i class="fa-brands fa-whatsapp"></i>
    </a>
    `;
    
    // Injeta o rodapé se a caixa (div) existir na página
    if (footerEl) {
        footerEl.innerHTML = appFooter;
    }
});
