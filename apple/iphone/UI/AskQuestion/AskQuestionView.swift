import SwiftUI
import MessageUI
import shared

private let contactsEmail: String = ContactsEmailKt.contactsEmail

struct AskQuestionView<Content: View>: View {
    
    let subject: String
    @ViewBuilder let content: () -> Content
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    @State private var mailViewResult: Result<MFMailComposeResult, Error>? = nil
    
    var body: some View {
        Button(
            action: {
                if MFMailComposeViewController.canSendMail() {
                    navigation.sheet {
                        MailView(
                            toEmail: contactsEmail,
                            subject: subject,
                            body: nil,
                            result: $mailViewResult
                        )
                    }
                } else {
                    let subjectEncoded = subject.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed)!
                    let url = URL(string: "mailto:\(contactsEmail)?subject=\(subjectEncoded)")!
                    UIApplication.shared.open(url)
                }
            },
            label: {
                content()
            }
        )
    }
}
