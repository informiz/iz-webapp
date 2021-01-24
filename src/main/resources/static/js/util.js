
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
