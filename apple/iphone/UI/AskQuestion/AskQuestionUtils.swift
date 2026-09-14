import SwiftUI
import MessageUI
import shared

private let contactsEmail: String = ContactsEmailKt.contactsEmail

struct AskQuestionUtils {
    
    @MainActor
    static func sendEmail(
        navigation: Navigation,
        subject: String,
        mailViewResult: Binding<Result<MFMailComposeResult, any Error>?>,
    ) {
        if MFMailComposeViewController.canSendMail() {
            navigation.sheet {
                MailView(
                    toEmail: contactsEmail,
                    subject: subject,
                    body: nil,
                    result: mailViewResult,
                )
            }
        } else {
            let subjectEncoded: String = subject.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed)!
            let url: URL = URL(string: "mailto:\(contactsEmail)?subject=\(subjectEncoded)")!
            UIApplication.shared.open(url)
        }
    }
}
