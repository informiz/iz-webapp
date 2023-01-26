package org.informiz.ctrl.checker;

import jakarta.servlet.http.HttpSession;
import org.informiz.model.ChainCodeEntity;
import org.informiz.model.FactCheckerBase;
import org.informiz.repo.CryptoUtils.ChaincodeProxy;


import static org.informiz.repo.CryptoUtils.ChaincodeProxy.PROXY_ATTR;

public class CheckerCCDao {

    public static FactCheckerBase addFactChecker(HttpSession session, FactCheckerBase checker) {
        String result = getCCProxy(session).submitTransaction("FactCheckerContract", "createFactChecker",
                new String[]{checker.getName(), checker.getScore().getReliability().toString(),
                        checker.getScore().getConfidence().toString(), checker.getEmail(), checker.getLink()});
        return ChainCodeEntity.fromEntityString(result, FactCheckerBase.class);
    }

    public static ChaincodeProxy getCCProxy(HttpSession session) {
        // TODO: re-create from encrypted blob?
        return (ChaincodeProxy) session.getAttribute(PROXY_ATTR);
    }


}
