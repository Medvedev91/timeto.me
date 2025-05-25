class ErrorApp: Error {

    let message: String

    init(_ message: String) {
        self.message = message
    }
}

extension Error {

    func messageApp() -> String {
        if let error = self as? ErrorApp {
            return error.message
        }
        return localizedDescription
    }
}
