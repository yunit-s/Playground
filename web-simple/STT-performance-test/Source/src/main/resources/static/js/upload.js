document.addEventListener('DOMContentLoaded', () => {
    const dropZone = document.getElementById('drop-zone');
    const fileInput = document.getElementById('fileInput');
    const fileList = document.getElementById('file-list');
    const uploadForm = document.getElementById('uploadForm');
    const fileInputLabel = document.querySelector('.file-input-label');

    let selectedFiles = []; // Array to hold File objects

    // Simulate click on hidden file input when label or drop zone is clicked
    fileInputLabel.addEventListener('click', () => fileInput.click());
    dropZone.addEventListener('click', () => fileInput.click());


    // Prevent default drag behaviors
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, preventDefaults, false);
        document.body.addEventListener(eventName, preventDefaults, false); // Prevent browser default for entire page
    });

    function preventDefaults(e) {
        e.preventDefault();
        e.stopPropagation();
    }

    // Highlight drop zone when dragging over
    ['dragenter', 'dragover'].forEach(eventName => {
        dropZone.addEventListener(eventName, () => dropZone.classList.add('highlight'), false);
    });

    ['dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, () => dropZone.classList.remove('highlight'), false);
    });

    // Handle dropped files
    dropZone.addEventListener('drop', handleDrop, false);

    function handleDrop(e) {
        const dt = e.dataTransfer;
        const files = dt.files;
        addFiles(files);
    }

    // Handle files selected via input
    fileInput.addEventListener('change', (e) => {
        const files = e.target.files;
        addFiles(files);
    });

    function addFiles(files) {
        // Convert FileList to Array and add to selectedFiles
        Array.from(files).forEach(file => {
            // Check for duplicates by name and size (simple check)
            const isDuplicate = selectedFiles.some(existingFile =>
                existingFile.name === file.name && existingFile.size === file.size
            );
            if (!isDuplicate) {
                selectedFiles.push(file);
            } else {
                console.warn(`Duplicate file ignored: ${file.name}`);
            }
        });
        renderFileList();
    }

    function renderFileList() {
        fileList.innerHTML = ''; // Clear current list
        selectedFiles.forEach((file, index) => {
            const li = document.createElement('li');
            li.innerHTML = `
                <span>${file.name} (${(file.size / 1024 / 1024).toFixed(2)} MB)</span>
                <button type="button" data-index="${index}" class="remove-file-btn">삭제</button>
            `;
            fileList.appendChild(li);
        });

        // Add event listeners for remove buttons
        document.querySelectorAll('.remove-file-btn').forEach(button => {
            button.addEventListener('click', (e) => {
                const indexToRemove = parseInt(e.target.dataset.index);
                selectedFiles.splice(indexToRemove, 1); // Remove file from array
                renderFileList(); // Re-render the list
            });
        });
    }

    // Handle form submission
    uploadForm.addEventListener('submit', (e) => {
        e.preventDefault(); // Prevent default form submission

        if (selectedFiles.length === 0) {
            alert('업로드할 파일을 선택해주세요.');
            return;
        }

        const formData = new FormData();
        selectedFiles.forEach(file => {
            formData.append('files', file); // 'files' must match @RequestParam("files") in controller
        });

        // Submit the form data using fetch API
        fetch(uploadForm.action, {
            method: 'POST',
            body: formData
        })
        .then(response => response.text()) // Assuming controller returns a redirect string or message
        .then(html => {
            // For simplicity, reload the page to show flash attributes
            // In a real app, you might parse the HTML or JSON response
            window.location.reload();
        })
        .catch(error => {
            console.error('Error during file upload:', error);
            alert('파일 업로드 중 오류가 발생했습니다.');
        });
    });
});
