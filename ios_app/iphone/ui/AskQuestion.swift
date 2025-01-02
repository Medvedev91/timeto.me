import SwiftUI
import MessageUI
import shared

struct AskQuestion<Content: View>: View {
    
    let subject: String
    @ViewBuilder let content: () -> Content
    
    @State private var isMailViewPresented = false
    @State private var mailViewResult: Result<MFMailComposeResult, Error>? = nil

    var body: some View {
        Button(
            action: {
                if (MFMailComposeViewController.canSendMail()) {
                    isMailViewPresented = true
                } else {
                    let subjectEncoded = subject.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed)!
                    let url = URL(string: "mailto:\(Utils_kmpKt.HI_EMAIL)?subject=\(subjectEncoded)")!
                    UIApplication.shared.open(url)
                }
            },
            label: {
                content()
            }
        )
        .sheetEnv(isPresented: $isMailViewPresented) {
            MailView(
                toEmail: Utils_kmpKt.HI_EMAIL,
                subject: subject,
                body: nil,
                result: $mailViewResult
            )
        }
    }
}
