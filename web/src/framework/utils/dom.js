export function offset (el) {
    if (el === window) {
        return { top: 0, left: 0 }
    }
    const { top, left } = el.getBoundingClientRect()
    return { top, left }
}
export function height (el) {
    return el === window
        ? window.innerHeight
        : el.getBoundingClientRect().height
}export function width (el) {
    return el === window
        ? window.innerWidth
        : el.getBoundingClientRect().width
}
