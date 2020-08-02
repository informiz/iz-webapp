
function processEntities(callbacks, endpoint, map) {
    if (callbacks.length > 0) {
        $.getJSON(endpoint, function(data) {
            $.each(data, function(i, entity) {
                map[entity.entityId] = entity;
            });
            $.each(callbacks, function(i, func) {
                func.apply();
            });
        });
    }
}

function processCheckers() {
    processEntities(checkersCallbacks, "/checker-api/all", checkers)
}

function processSources() {
    processEntities(sourcesCallbacks, "/source-api/all", sources)
}

function processClaims() {
    processEntities(claimsCallbacks, "/claim-api/all", claims)
}

function processCitations() {
    processEntities(citationsCallbacks, "/citation-api/all", citations)
}

function processInformiz() {
    processEntities(informizCallbacks, "/informi-api/all", informiz)
}


$(document).ready(function() {
    processCheckers();
    processSources();
    processClaims();
    processCitations();
    processInformiz();
});
