function searchTrainingVideos() {
    const trainingName = document.getElementById('trainingName')?.value.trim();
    if (!trainingName) {
        document.getElementById('trainingName')?.focus();
        return;
    }
    const query = encodeURIComponent(`${trainingName} training lesson`);
    window.open(`https://www.youtube.com/results?search_query=${query}`, '_blank', 'noopener,noreferrer');
}