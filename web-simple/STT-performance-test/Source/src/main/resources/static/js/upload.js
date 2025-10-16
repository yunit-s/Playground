document.addEventListener('DOMContentLoaded', () => {
    const uploadForm = document.getElementById('uploadForm');

    // State for selected files
    let selectedWavFiles = [];
    let selectedTxtFiles = [];

    // Setup for WAV drop zone
    setupDropZone(
        document.getElementById('wav-drop-zone'),
        document.getElementById('wavFiles'),
        document.getElementById('wav-file-list'),
        selectedWavFiles,
        ['audio/wav', 'audio/mpeg'] // Accepts .wav, .mp3
    );

    // Setup for TXT drop zone
    setupDropZone(
        document.getElementById('txt-drop-zone'),
        document.getElementById('txtFiles'),
        document.getElementById('txt-file-list'),
        selectedTxtFiles,
        ['text/plain'] // Accepts .txt
    );

    /**
     * Generic function to set up a drag-and-drop zone.
     * @param {HTMLElement} dropZone - The drop zone element.
     * @param {HTMLInputElement} fileInput - The hidden file input.
     * @param {HTMLElement} fileListElement - The <ul> element to display the file list.
     * @param {File[]} selectedFiles - The array to store selected files.
     * @param {string[]} allowedTypes - Array of allowed MIME types.
     */
    function setupDropZone(dropZone, fileInput, fileListElement, selectedFiles, allowedTypes) {
        // Simulate click on hidden file input when drop zone is clicked
        dropZone.addEventListener('click', () => fileInput.click());

        // Prevent default drag behaviors
        ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
            dropZone.addEventListener(eventName, preventDefaults, false);
            document.body.addEventListener(eventName, preventDefaults, false);
        });

        function preventDefaults(e) {
            e.preventDefault();
            e.stopPropagation();
        }

        // Highlight drop zone
        ['dragenter', 'dragover'].forEach(eventName => {
            dropZone.addEventListener(eventName, () => dropZone.classList.add('highlight'), false);
        });

        ['dragleave', 'drop'].forEach(eventName => {
            dropZone.addEventListener(eventName, () => dropZone.classList.remove('highlight'), false);
        });

        // Handle dropped files
        dropZone.addEventListener('drop', (e) => {
            const dt = e.dataTransfer;
            const files = dt.files;
            addFiles(files);
        }, false);

        // Handle files selected via input
        fileInput.addEventListener('change', (e) => {
            const files = e.target.files;
            addFiles(files);
        });

        function addFiles(files) {
            Array.from(files).forEach(file => {
                if (allowedTypes.some(type => file.type.startsWith(type) || (type === 'text/plain' && file.name.endsWith('.txt')))) {
                    const isDuplicate = selectedFiles.some(f => f.name === file.name && f.size === file.size);
                    if (!isDuplicate) {
                        selectedFiles.push(file);
                    } else {
                        console.warn(`Duplicate file ignored: ${file.name}`);
                    }
                } else {
                    showToast(`잘못된 파일 형식: ${file.name}. 허용 형식: ${allowedTypes.join(', ')}`, 'error');
                }
            });
            renderFileList();
        }

        function renderFileList() {
            fileListElement.innerHTML = '';
            selectedFiles.forEach((file, index) => {
                const li = document.createElement('li');
                li.innerHTML = `
                    <span>${file.name} (${(file.size / 1024 / 1024).toFixed(2)} MB)</span>
                    <button type="button" data-index="${index}" class="remove-file-btn">삭제</button>
                `;
                fileListElement.appendChild(li);
            });

            // Add event listeners for remove buttons
            fileListElement.querySelectorAll('.remove-file-btn').forEach(button => {
                button.addEventListener('click', (e) => {
                    const indexToRemove = parseInt(e.target.dataset.index, 10);
                    selectedFiles.splice(indexToRemove, 1);
                    renderFileList();
                });
            });
        }
    }

    // Handle form submission
    uploadForm.addEventListener('submit', (e) => {
        if (selectedWavFiles.length === 0 && selectedTxtFiles.length === 0) {
            showToast('업로드할 파일을 선택해주세요.', 'error');
            e.preventDefault();
            return;
        }

        // Combine all files into one list for submission
        const allFiles = [...selectedWavFiles, ...selectedTxtFiles];
        const dataTransfer = new DataTransfer();
        allFiles.forEach(file => dataTransfer.items.add(file));

        // Assign the combined file list to one of the inputs for submission
        // The backend will receive all files under the 'files' parameter name
        document.getElementById('wavFiles').files = dataTransfer.files;
    });
});