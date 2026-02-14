import Foundation

func myAsync(
    _ function: @escaping () -> Void
) {
    DispatchQueue.main.async {
        function()
    }
}

func myAsyncAfter(
    _ seconds: CGFloat,
    work: @escaping () -> Void
) {
    DispatchQueue.main.asyncAfter(deadline: .now() + seconds) {
        work()
    }
}
