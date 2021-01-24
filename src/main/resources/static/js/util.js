
function tableHeaderStyle(column) {
    return {
      css: { 'background-color': '#28A745', 'color': 'white' }
    }
}

const options = {year: 'numeric', month: 'numeric', day: 'numeric' };

function timestampsToStrings(elements) {
    var locale = navigator.language || Intl.DateTimeFormat().resolvedOptions().locale || "en-US";
    elements.each(function() {
        d = new Date();
        ts = parseInt($(this).text());
        d.setTime(ts);
        $(this).text(d.toLocaleString(locale, options));
    });
}

function checkersToLinks(elements) {
    var locale = navigator.language || Intl.DateTimeFormat().resolvedOptions().locale || "en-US";
    elements.each(function() {
        entityId = $(this).attr('data-checker-id');
        checker = checkers[entityId];
        if (checker)
            $(this).append('<a href = "/factchecker/view/' + checker.id + '">' + checker.name + '</a>')
    });
}

// TODO: similar issue for source/reference/review lists
function handleTableRender(table) {
    table.on('post-body.bs.table', function (e, data) {
        timestampsToStrings($('td[title="timestamp"]'));
        checkersToLinks($('td[title="fact-checker"]'));
    });
}
