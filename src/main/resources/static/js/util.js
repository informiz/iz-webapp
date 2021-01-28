
function tableHeaderStyle(column) {
    return {
      css: { 'background-color': '#28A745', 'color': 'white' }
    }
}

function tableHeaderHide(column) {
    return {
      css: { 'display': 'none' }
    }
}

const options = {year: 'numeric', month: 'numeric', day: 'numeric' };

var locale = navigator.language || Intl.DateTimeFormat().resolvedOptions().locale || "en-US";

function timestampsToStrings(elements) {
    elements.each(function() {
        d = new Date();
        ts = parseInt($(this).text());
        d.setTime(ts);
        $(this).text(d.toLocaleString(locale, options));
    });
}

function checkersToLinks(elements) {
    elements.each(function() {
        entityId = $(this).attr('data-checker-id');
        checker = checkers[entityId];
        if (checker)
            $(this).append('<a href = "/factchecker/view/' + checker.id + '">' + checker.name + '</a>')
    });
}

function handleTableRender(table) {
    // TODO: on first table rendering the checkers are not loaded yet. Find a better solution
    checkersCallbacks.push(function() {
        checkersToLinks($('td[title="fact-checker"]'));
    });

    table.on('post-body.bs.table', function (e, data) {
        timestampsToStrings($('[title="timestamp"]'));
        checkersToLinks($('[title="fact-checker"]'));
    });
}

function renderReferences() {
    $('td[data-reference]').val(function() {
        refId = $(this).attr('data-reference');

        if (citations[refId]) {
            ref = citations[refId];
            $(this).html('<span class="table-text"><a href = "/citation/view/' + ref.id + '" title="' + ref.text + '">' + ref.text + '</a></span>')
        }
        else if (claims[refId]) {
            ref = claims[refId];
            $(this).html('<span class="table-text"><a href = "/hypothesis/view/' + ref.id + '" title="' + ref.claim + '">' + ref.claim + '</a></span>')
        }
    });
}

function handleReferencesRender(table) {
    // TODO: Dummy function to trigger claims and citations loading. Temporary solution while loading from local channel
    claimsCallbacks.push(function() {});
    citationsCallbacks.push(function() {});

    // TODO: on first table rendering the claims and citations are not loaded yet. Better solution?
    loadCompleteCallbacks.push(function() {
        renderReferences();
    });

    table.on('post-body.bs.table', function (e, data) {
        renderReferences();
    });
}


