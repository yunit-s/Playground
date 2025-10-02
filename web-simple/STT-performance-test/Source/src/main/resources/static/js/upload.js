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
        if (selectedFiles.length === 0) {
            alert('업로드할 파일을 선택해주세요.');
            e.preventDefault(); // 파일이 없을 경우에만 제출 방지
            return;
        }

        // selectedFiles 배열의 파일들을 실제 fileInput 요소에 할당하여 폼이 자연스럽게 제출되도록 합니다.
        const dataTransfer = new DataTransfer();
        selectedFiles.forEach(file => dataTransfer.items.add(file));
        fileInput.files = dataTransfer.files;

        // 이제 폼이 자연스럽게 제출되도록 e.preventDefault()를 제거합니다.
        // 폼이 제출되면 서버에서 리다이렉트 응답을 보내고, 브라우저가 이를 따라갑니다.
        // flash attribute는 이 과정에서 유지됩니다.
    });
});