import SwiftUI
import MessageUI
import shared

struct AskAQuestionButtonView: View {

    let subject: String
    let isFirst: Bool
    let isLast: Bool
    var bgColor: Color = c.sheetFg
    let withTopDivider: Bool

    @State private var isMailViewPresented = false
    @State private var mailViewResult: Result<MFMailComposeResult, Error>? = nil

    var body: some View {

        MyListView__ItemView(
            isFirst: isFirst,
            isLast: isLast,
            bgColor: bgColor,
            withTopDivider: withTopDivider
        ) {

            MyListView__ItemView__ButtonView(text: "Ask a Question") {
                if (MFMailComposeViewController.canSendMail()) {
                    isMailViewPresented.toggle()
                } else {
                    // Взято из skorolek
                    let subjectEncoded = subject.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed)!
                    let url = URL(string: "mailto:\(Utils_kmpKt.HI_EMAIL)?subject=\(subjectEncoded)")!
                    UIApplication.shared.open(url)
                }
            }
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
}
