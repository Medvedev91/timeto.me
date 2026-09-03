import UIKit
import WatchConnectivity
import shared

class AppDelegate: NSObject, UIApplicationDelegate {
    
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        
        // todo remove, migration starts 1 Sep 2026
        do {
            let fileManager = FileManager.default
            var fileURLs: [URL] = []
            let dir1: URL = URL.applicationSupportDirectory
            if (try? dir1.resourceValues(forKeys: [.isDirectoryKey]).isDirectory) == true {
                fileURLs.append(contentsOf: try fileManager.contentsOfDirectory(at: dir1, includingPropertiesForKeys: nil))
                let dir2 = dir1.appendingPathComponent("databases")
                if (try? dir2.resourceValues(forKeys: [.isDirectoryKey]).isDirectory) == true {
                    fileURLs.append(contentsOf: try fileManager.contentsOfDirectory(at: dir2, includingPropertiesForKeys: nil))
                }
            }
            try fileURLs.forEach { oldDbUrl in
                let name: String = oldDbUrl.lastPathComponent
                if name == "timetome.db" {
                    print("timetome.db: move \(oldDbUrl.lastPathComponent) \(oldDbUrl)")
                    
                    let newDbFolderUrl: URL = fileManager.containerURL(forSecurityApplicationGroupIdentifier: "group.me.timeto.app")!
                    let newDbUrl: URL = newDbFolderUrl.appendingPathComponent(name)
                    print("timetome.db: New DB Url \(newDbUrl)")
                    
                    if FileManager.default.fileExists(atPath: newDbUrl.path) {
                        print("timetome.db: File Exists")
                        try FileManager.default.removeItem(atPath: newDbUrl.path)
                    }
                    try FileManager.default.moveItem(atPath: oldDbUrl.path, toPath: newDbUrl.path)
                    print("timetome.db: Migration Complete")
                }
            }
        } catch {
            Task {
                // await for init KMP
                try? await Task.sleep(nanoseconds: 2_000_000_000)
                reportApi("AppDelegate.application() move timetome.db error \(error)")
            }
        }
        
        InitKmpIosKt.doInitKmpIos()
        setupWCSession(self)
        return true
    }
    
    func applicationSignificantTimeChange(_ application: UIApplication) {
        LocalUtcOffsetKt.localUtcOffsetSync()
    }
    
    func application(
        _ application: UIApplication,
        supportedInterfaceOrientationsFor window: UIWindow?,
    ) -> UIInterfaceOrientationMask {
        OrientationManager.instance.orientationMask
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
