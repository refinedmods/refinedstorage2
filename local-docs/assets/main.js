const expandButton = document.querySelector('#nav-expand-button')
if (expandButton) {
    const collapsableNavItems = document.querySelectorAll('.nav-collapse')

    function isAllExpanded() {
        return Array.from(collapsableNavItems).every(toggle => toggle.classList.contains('show'))
    }

    const expandIcon = expandButton.querySelector('.bi')
    expandButton.addEventListener('click', () => {
        if (isAllExpanded()) {
            collapsableNavItems.forEach(btn => btn.classList.remove('show'))
            document.querySelectorAll('.btn-toggle').forEach(btn => {
                btn.setAttribute('aria-expanded', 'false')
                btn.classList.remove('collapsed')
            })
        } else {
            collapsableNavItems.forEach(btn => btn.classList.add('show'))
            document.querySelectorAll('.btn-toggle').forEach(btn => {
                btn.setAttribute('aria-expanded', 'true')
                btn.classList.add('collapsed')
            })
        }
        updateExpandedState(isAllExpanded())
    })

    collapsableNavItems.forEach(btn => {
        btn.addEventListener('shown.bs.collapse', () => {
            updateExpandedState(isAllExpanded())
        })
        btn.addEventListener('hidden.bs.collapse', () => {
            updateExpandedState(false)
        })
    });

    updateExpandedState(isAllExpanded())

    function updateExpandedState(allExpanded) {
        if (expandIcon.classList.contains('bi-arrows-expand')) {
            expandIcon.classList.remove('bi-arrows-expand')
        }
        if (expandIcon.classList.contains('bi-arrows-collapse')) {
            expandIcon.classList.remove('bi-arrows-collapse')
        }
        expandIcon.classList.add(allExpanded ? 'bi-arrows-collapse' : 'bi-arrows-expand')
        expandButton.setAttribute('aria-label', allExpanded ? 'Collapse all' : 'Expand all')
    }
}

document.querySelector('main').querySelectorAll('h1, h2, h3, h4, h5, h6').forEach(item => {
    if (!item.hasAttribute('id')) {
        return
    }
    const anchor = document.createElement('a')
    anchor.setAttribute('href', '#' + item.id)
    anchor.classList.add('invisible')
    anchor.style.paddingLeft = '5px'
    anchor.innerHTML = '<i class="bi bi-link"></i>'
    item.addEventListener('mouseenter', () => {
        anchor.classList.remove('invisible')
    });
    item.addEventListener('mouseleave', () => {
        anchor.classList.add('invisible')
    });
    item.appendChild(anchor)
})

tippy('a[data-icon-id]', {
    content(reference) {
        const id = reference.getAttribute('data-icon-id')
        const template = document.getElementById('icon-' + id)
        return template.innerHTML
    },
    placement: 'right',
    theme: 'material',
    allowHTML: true,
})
