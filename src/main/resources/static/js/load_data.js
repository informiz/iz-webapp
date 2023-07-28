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

    let deferred = $.when(p1, p2, p3, p4, p5).done(function (res1, res2, res3, res4, res5) {
        $.each(loadCompleteCallbacks, function (i, func) {
            func.apply();
        });
    });

    let deferred2 = null;
    if (autoSearchCallbacks.length > 0) {
        deferred2 = $.when(deferred).done(function (res) {
            $.each(autoSearchCallbacks, function(i, func) { func.apply(); })
        });
    }

    if (modalCallbacks.length > 0) {
        let last_process = deferred2 ? deferred2 : deferred;
        $.when(last_process).done(function (res) {
            $.each(modalCallbacks, function(i, func) { func.apply(); })
        });
    }
});
