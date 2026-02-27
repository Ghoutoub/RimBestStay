// /resources/static/js/search.js

document.addEventListener('DOMContentLoaded', function() {
    // ============ VARIABLES GLOBALES ============
    let currentSearchParams = {};
    let isAdvancedFiltersVisible = false;
    
    // ============ INITIALISATION ============
    initDatePickers();
    initAutocomplete();
    initAdvancedFilters();
    initSorting();
    initFilterSidebar();
    
    // ============ GESTION DES DATES ============
    function initDatePickers() {
        const today = new Date().toISOString().split('T')[0];
        const tomorrow = new Date(Date.now() + 86400000).toISOString().split('T')[0];
        
        const arrivalInput = document.querySelector('input[name="dateArrivee"]');
        const departureInput = document.querySelector('input[name="dateDepart"]');
        
        if (arrivalInput && !arrivalInput.value) {
            arrivalInput.value = today;
            arrivalInput.min = today;
        }
        
        if (departureInput && !departureInput.value) {
            departureInput.value = tomorrow;
            departureInput.min = tomorrow;
        }
        
        // Mettre à jour la date min de départ quand l'arrivée change
        if (arrivalInput) {
            arrivalInput.addEventListener('change', function() {
                if (departureInput) {
                    const arrivalDate = new Date(this.value);
                    const nextDay = new Date(arrivalDate.getTime() + 86400000);
                    departureInput.min = nextDay.toISOString().split('T')[0];
                    
                    if (new Date(departureInput.value) < nextDay) {
                        departureInput.value = nextDay.toISOString().split('T')[0];
                    }
                }
            });
        }
    }
    
    // ============ AUTOCOMPLÉTION ============
    function initAutocomplete() {
        const destinationInput = document.querySelector('input[name="ville"]');
        if (!destinationInput) return;
        
        let timeoutId;
        destinationInput.addEventListener('input', function() {
            clearTimeout(timeoutId);
            
            if (this.value.length < 2) {
                hideAutocomplete();
                return;
            }
            
            timeoutId = setTimeout(() => {
                fetchAutocomplete(this.value);
            }, 300);
        });
        
        // Cacher l'autocomplete quand on clique ailleurs
        document.addEventListener('click', function(e) {
            if (!e.target.closest('.autocomplete-container')) {
                hideAutocomplete();
            }
        });
    }
    
    function fetchAutocomplete(query) {
        fetch(`/search/autocomplete?q=${encodeURIComponent(query)}`)
            .then(response => response.json())
            .then(suggestions => {
                showAutocomplete(suggestions);
            })
            .catch(error => console.error('Erreur autocomplete:', error));
    }
    
    function showAutocomplete(suggestions) {
        hideAutocomplete();
        
        if (suggestions.length === 0) return;
        
        const container = document.createElement('div');
        container.className = 'autocomplete-container dropdown-menu show';
        container.style.position = 'absolute';
        container.style.width = '100%';
        
        suggestions.forEach(suggestion => {
            const item = document.createElement('button');
            item.type = 'button';
            item.className = 'dropdown-item autocomplete-item';
            item.textContent = suggestion;
            item.addEventListener('click', function() {
                document.querySelector('input[name="ville"]').value = suggestion;
                hideAutocomplete();
            });
            container.appendChild(item);
        });
        
        const inputGroup = document.querySelector('input[name="ville"]').closest('.input-group');
        inputGroup.appendChild(container);
    }
    
    function hideAutocomplete() {
        const existing = document.querySelector('.autocomplete-container');
        if (existing) existing.remove();
    }
    
    // ============ FILTRES AVANCÉS ============
    function initAdvancedFilters() {
        const toggleBtn = document.getElementById('toggleAdvancedFilters');
        if (!toggleBtn) return;
        
        toggleBtn.addEventListener('click', function() {
            const filtersDiv = document.getElementById('advancedFilters');
            if (!filtersDiv) return;
            
            if (isAdvancedFiltersVisible) {
                filtersDiv.style.display = 'none';
                this.innerHTML = '<i class="bi bi-sliders"></i> Plus de filtres';
            } else {
                filtersDiv.style.display = 'block';
                this.innerHTML = '<i class="bi bi-sliders"></i> Moins de filtres';
            }
            
            isAdvancedFiltersVisible = !isAdvancedFiltersVisible;
        });
        
        // Gestion des étoiles
        document.querySelectorAll('.btn-star').forEach(btn => {
            btn.addEventListener('click', function() {
                const stars = this.dataset.stars;
                document.querySelectorAll('.btn-star').forEach(b => b.classList.remove('active'));
                this.classList.add('active');
                document.getElementById('etoilesMin').value = stars;
            });
        });
    }
    
    // ============ TRI DES RÉSULTATS ============
    function initSorting() {
        const sortSelect = document.getElementById('sortSelect');
        if (!sortSelect) return;
        
        sortSelect.addEventListener('change', function() {
            const form = document.getElementById('searchForm');
            if (!form) return;
            
            // Créer un champ caché pour le tri
            let sortInput = form.querySelector('input[name="triPar"]');
            if (!sortInput) {
                sortInput = document.createElement('input');
                sortInput.type = 'hidden';
                sortInput.name = 'triPar';
                form.appendChild(sortInput);
            }
            sortInput.value = this.value;
            
            // Soumettre le formulaire
            form.submit();
        });
    }
    
    // ============ SIDEBAR DES FILTRES ============
    function initFilterSidebar() {
        const filterSidebar = document.getElementById('filtersSidebar');
        if (!filterSidebar) return;
        
        // Récupérer les filtres depuis l'API
        fetchFilters();
    }
    
    function fetchFilters() {
        fetch('/search/api/filters')
            .then(response => response.json())
            .then(filters => {
                renderFilters(filters);
            })
            .catch(error => console.error('Erreur chargement filtres:', error));
    }
    
    function renderFilters(filters) {
        const container = document.querySelector('#filtersSidebar .card-body');
        if (!container) return;
        
        let html = '';
        
        // Filtre par ville
        if (filters.cities) {
            html += '<div class="mb-4">';
            html += '<h6 class="fw-bold mb-2"><i class="bi bi-geo-alt me-2"></i>Destination</h6>';
            filters.cities.forEach(city => {
                html += `
                    <div class="form-check">
                        <input class="form-check-input city-filter" type="checkbox" 
                               value="${city.name}" id="city-${city.id}">
                        <label class="form-check-label" for="city-${city.id}">
                            ${city.name} <span class="text-muted">(${city.count})</span>
                        </label>
                    </div>
                `;
            });
            html += '</div>';
        }
        
        // Filtre par étoiles
        if (filters.stars) {
            html += '<div class="mb-4">';
            html += '<h6 class="fw-bold mb-2"><i class="bi bi-star me-2"></i>Classement</h6>';
            for (let i = 5; i >= 1; i--) {
                const count = filters.stars[i] || 0;
                html += `
                    <div class="form-check">
                        <input class="form-check-input star-filter" type="checkbox" 
                               value="${i}" id="star-${i}">
                        <label class="form-check-label" for="star-${i}">
                            ${'★'.repeat(i)} <span class="text-muted">(${count})</span>
                        </label>
                    </div>
                `;
            }
            html += '</div>';
        }
        
        // Filtre par équipements
        html += `
            <div class="mb-4">
                <h6 class="fw-bold mb-2"><i class="bi bi-wifi me-2"></i>Équipements</h6>
                <div class="form-check">
                    <input class="form-check-input equipment-filter" type="checkbox" value="WIFI" id="filter-wifi">
                    <label class="form-check-label" for="filter-wifi">WiFi gratuit</label>
                </div>
                <div class="form-check">
                    <input class="form-check-input equipment-filter" type="checkbox" value="POOL" id="filter-pool">
                    <label class="form-check-label" for="filter-pool">Piscine</label>
                </div>
                <div class="form-check">
                    <input class="form-check-input equipment-filter" type="checkbox" value="SPA" id="filter-spa">
                    <label class="form-check-label" for="filter-spa">SPA</label>
                </div>
                <div class="form-check">
                    <input class="form-check-input equipment-filter" type="checkbox" value="PARKING" id="filter-parking">
                    <label class="form-check-label" for="filter-parking">Parking</label>
                </div>
            </div>
        `;
        
        container.innerHTML = html;
        
        // Écouter les changements de filtres
        document.querySelectorAll('.city-filter, .star-filter, .equipment-filter').forEach(checkbox => {
            checkbox.addEventListener('change', applyFilters);
        });
    }
    
    function applyFilters() {
        const form = document.getElementById('searchForm');
        if (!form) return;
        
        // Collecter les filtres
        const selectedCities = Array.from(document.querySelectorAll('.city-filter:checked'))
            .map(cb => cb.value);
        const selectedStars = Array.from(document.querySelectorAll('.star-filter:checked'))
            .map(cb => parseInt(cb.value));
        const selectedEquipments = Array.from(document.querySelectorAll('.equipment-filter:checked'))
            .map(cb => cb.value);
        
        // Mettre à jour les champs cachés
        updateHiddenField(form, 'cities', selectedCities);
        updateHiddenField(form, 'etoiles', selectedStars);
        updateHiddenField(form, 'equipementsHotel', selectedEquipments);
        
        // Soumettre le formulaire
        form.submit();
    }
    
    function updateHiddenField(form, name, values) {
        // Supprimer les anciens champs
        form.querySelectorAll(`input[name="${name}"]`).forEach(input => input.remove());
        
        // Ajouter les nouveaux
        values.forEach(value => {
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = name;
            input.value = value;
            form.appendChild(input);
        });
    }
    
    // ============ PAGINATION AJAX ============
    function initAjaxPagination() {
        document.querySelectorAll('.page-link').forEach(link => {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                
                const page = this.dataset.page;
                if (!page) return;
                
                loadPage(page);
            });
        });
    }
    
    function loadPage(page) {
        const form = document.getElementById('searchForm');
        if (!form) return;
        
        // Mettre à jour le numéro de page
        let pageInput = form.querySelector('input[name="page"]');
        if (!pageInput) {
            pageInput = document.createElement('input');
            pageInput.type = 'hidden';
            pageInput.name = 'page';
            form.appendChild(pageInput);
        }
        pageInput.value = page;
        
        // Soumettre le formulaire
        form.submit();
    }
    
    // ============ MISE À JOUR DES VOYAGEURS ============
    function updateTravelersText() {
        const adults = document.querySelector('input[name="adultes"]').value || 1;
        const children = document.querySelector('input[name="enfants"]').value || 0;
        const rooms = document.querySelector('input[name="chambres"]').value || 1;
        
        let text = `${adults} ${adults > 1 ? 'adultes' : 'adulte'}`;
        if (children > 0) {
            text += `, ${children} ${children > 1 ? 'enfants' : 'enfant'}`;
        }
        if (rooms > 1) {
            text += `, ${rooms} chambres`;
        } else {
            text += `, 1 chambre`;
        }
        
        document.getElementById('voyageursText').textContent = text;
    }
    
    // Initialiser le texte des voyageurs
    const adultInput = document.querySelector('input[name="adultes"]');
    const childInput = document.querySelector('input[name="enfants"]');
    const roomInput = document.querySelector('input[name="chambres"]');
    
    if (adultInput && childInput && roomInput) {
        [adultInput, childInput, roomInput].forEach(input => {
            input.addEventListener('change', updateTravelersText);
        });
        updateTravelersText();
    }
});