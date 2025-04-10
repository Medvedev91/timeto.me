import shared

let Cache = shared.Cache.shared

func zlog(_ message: Any?) {
    UtilsKt.zlog(message: message)
}

func time() -> Int {
    TimeKt.time().toInt()
}

func reportApi(_ message: String) {
    Utils_kmpKt.reportApi(message: message, force: false)
}
