export const getPaginationRange = (currentPage, totalPages) => {
    const range = [];
    const rangesWithDots = []

    console.log(currentPage);

    range.push(1);

    for(let i = currentPage - 2; i <= currentPage + 2; i++) {
        if(i < totalPages && i > 1) range.push(i);
    }

    if(totalPages > 1) range.push(totalPages);

    let lastValue = undefined;
    for(let page of range) {
        if(lastValue !== undefined) {
            if(page - lastValue > 1) rangesWithDots.push("...");
        }
        rangesWithDots.push(page);
        lastValue = page;
    }

    return rangesWithDots;
}