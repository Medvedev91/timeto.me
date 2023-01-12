import WatchKit
import WatchConnectivity
import shared

class W_Delegate: NSObject, WKApplicationDelegate {

    func applicationDidFinishLaunching() {
        setupWCSession(self)
    }
}

extension W_Delegate: WCSessionDelegate {

    func session(
            _ session: WCSession,
            activationDidCompleteWith activationState: WCSessionActivationState,
            error: Error?
    ) {
        // todo log if error != nil
    }

    func session(
            _ session: WCSession,
            didReceiveApplicationContext applicationContext: [String: Any]
    ) {
        let backupJString = applicationContext["backup"] as! String
        WatchToIosSync.shared.smartRestore(jsonString: backupJString)
    }
}
