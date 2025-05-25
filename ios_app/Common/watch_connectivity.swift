import WatchConnectivity

func setupWCSession(_ delegate: WCSessionDelegate) {
    if WCSession.isSupported() {
        let session = WCSession.default
        session.delegate = delegate
        session.activate()
    } else {
        zlog("setupWCSession is not supported")
    }
}
