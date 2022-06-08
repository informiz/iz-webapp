function tableHeaderStyle(column) {
    return {
      css: { 'background-color': '#28A745', 'color': 'white' }
    };
}

dateOptions = {year: 'numeric', month: 'long', day: 'numeric' };

var locale = navigator.language || Intl.DateTimeFormat().resolvedOptions().locale || "en-US";


function tsToString(value) {
    d = new Date();
    ts = parseInt(value);
    d.setTime(ts);
    return d.toLocaleString(locale, dateOptions);
}


function timestampsToStrings(elements) {
    elements.each(function() {
        ts = $(this).attr('data-iz-ts');
        $(this).text(tsToString(ts));
    });
}

function checkerToLink(value) {
    checker = checkers[value];
    if (checker)
        return '<a href = "/factchecker/view/' + checker.id + '">' + checker.name + '</a>';

    return value;
}

function checkersToLinks(elements) {
    elements.each(function() {
        checkerId = $(this).attr('data-checker-id');
        $(this).html(checkerToLink(checkerId));
    });
}
