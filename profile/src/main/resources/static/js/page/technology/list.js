document.addEventListener('DOMContentLoaded', () => {
    initTechnologyLevelForms()
    initAddTechnologiesModal()
});

function initTechnologyLevelForms() {

    const successMessage = document.getElementById('technology-level-saved-message');
    const closeSuccessMessage = successMessage.querySelector('[data-technology-level-saved-message-close]');

    closeSuccessMessage.addEventListener('click', () => successMessage.classList.add('d-none'));

    document.querySelectorAll('select[name="level"][form]')
        .forEach(levelSelect => {
            let initialLevel = levelSelect.value;
            let saving = false;
            const form = document.getElementById(levelSelect.getAttribute('form'));
            const saveButton = form.querySelector('[data-technology-level-save]');

            const updateStateSaveButton = () => {
                saveButton.disabled = saving || levelSelect.value === initialLevel;
            };

            levelSelect.addEventListener('change', updateStateSaveButton);

            form.addEventListener('submit', async event => {
                event.preventDefault();
                const levelToSave = levelSelect.value;
                saving = true;
                updateStateSaveButton();

                try {
                    await fetch(form.dataset.apiUrl, {
                        method: 'PATCH',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify(levelToSave)
                    }).then(response => process(response));
                    initialLevel = levelToSave;
                    successMessage.classList.remove('d-none');
                } catch (error) {
                    console.error(error);
                } finally {
                    saving = false;
                    updateStateSaveButton();
                }
            });
        });
}

function initAddTechnologiesModal() {
    const modalElement = document.getElementById('addTechnologiesModal');
    if (!modalElement) {
        return;
    }

    const modal = bootstrap.Modal.getOrCreateInstance(modalElement);
    const shouldShow = modalElement.dataset.show === 'true';

    modalElement.addEventListener('hidden.bs.modal', function () {
        if (shouldShow && window.location.search) {
            window.location.replace(window.location.pathname);
        }
    });

    if (shouldShow) {
        modal.show();
    }
}

async function process(response) {
    if (response.status === 401) {
        window.location.assign('/login');
        return response;
    }
    if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
    }
    return response;
}
