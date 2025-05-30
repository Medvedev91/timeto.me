import UIKit
import WatchConnectivity
import shared

class AppDelegate: NSObject, UIApplicationDelegate {
    
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        InitKmpIosKt.doInitKmpIos()
        setupWCSession(self)
        return true
    }
}

//
// https://www.youtube.com/watch?v=_Gkp3H6Mnfs

extension AppDelegate: WCSessionDelegate {
    
    func session(
        _ session: WCSession,
        activationDidCompleteWith activationState: WCSessionActivationState,
        error: Error?
    ) {
        // todo log if error != nil
    }
    
    func sessionDidBecomeInactive(_ session: WCSession) {
    }
    
    // todo what is it?
    func sessionDidDeactivate(_ session: WCSession) {
        WCSession.default.activate()
    }
    
    public func session(
        _ session: WCSession,
        didReceiveMessageData messageData: Data,
        replyHandler: @escaping (Data) -> ()
    ) {
        // Otherwise a lot of logs about background
        DispatchQueue.main.async {
            IosToWatchSync.shared.didReceiveMessageData(
                jString: String(decoding: messageData, as: UTF8.self)
            ) { jRes in
                replyHandler(Data(jRes.utf8))
            }
        }
    }
}
