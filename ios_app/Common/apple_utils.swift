import shared

let Cache = shared.Cache.shared

func reportApi(_ message: String) {
    Utils_kmpKt.reportApi(message: message, force: false)
}
