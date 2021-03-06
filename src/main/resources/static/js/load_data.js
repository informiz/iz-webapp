
function processEntities(callbacks, endpoint, map) {

    if (callbacks.length > 0) {
        promise = $.getJSON(endpoint, function(data) {
            $.each(data, function(i, entity) {
                map[entity.entityId] = entity;
            });
            $.each(callbacks, function(i, func) {
                func.apply();
            });
        });
        return promise;
    }
}

function processCheckers() {
    return processEntities(checkersCallbacks, "/checker-api/all", checkers)
}

function processSources() {
    return processEntities(sourcesCallbacks, "/source-api/all", sources)
}

function processClaims() {
    return processEntities(claimsCallbacks, "/claim-api/all", claims)
}

function processCitations() {
    return processEntities(citationsCallbacks, "/citation-api/all", citations)
}

function processInformiz() {
    return processEntities(informizCallbacks, "/informi-api/all", informiz)
}


$(document).ready(function() {
    p1 = processCheckers();
    p2 = processSources();
    p3 = processClaims();
    p4 = processCitations();
    p5 = processInformiz();

    if (loadCompleteCallbacks.length > 0) {
        $.when( p1, p2, p3, p4, p5 ).done(function ( res1, res2, res3, res4, res5 ) {
            $.each(loadCompleteCallbacks, function(i, func) {
                func.apply();
            });
        });
    }
});
