import WatchKit
import WatchConnectivity
import shared

class WatchDelegate: NSObject, WKApplicationDelegate {
    
    func applicationDidFinishLaunching() {
        setupWCSession(self)
    }
}

extension WatchDelegate: WCSessionDelegate {
    
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
