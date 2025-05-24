import SwiftUI
import MessageUI

//
// https://stackoverflow.com/a/58693164
 
struct MailView: UIViewControllerRepresentable {
    
    let toEmail: String
    let subject: String
    let body: String?
    @Binding var result: Result<MFMailComposeResult, Error>?
    
    @Environment(\.presentationMode) private var presentation
    
    class Coordinator: NSObject, MFMailComposeViewControllerDelegate {
        
        @Binding var presentation: PresentationMode
        @Binding var result: Result<MFMailComposeResult, Error>?
        
        init(
            presentation: Binding<PresentationMode>,
            result: Binding<Result<MFMailComposeResult, Error>?>
        ) {
            _presentation = presentation
            _result = result
        }
        
        func mailComposeController(
            _ controller: MFMailComposeViewController,
            didFinishWith result: MFMailComposeResult,
            error: Error?
        ) {
            defer {
                $presentation.wrappedValue.dismiss()
            }
            guard error == nil else {
                self.result = .failure(error!)
                return
            }
            self.result = .success(result)
        }
    }
    
    func makeCoordinator() -> Coordinator {
        Coordinator(presentation: presentation, result: $result)
    }
    
    func makeUIViewController(
        context: UIViewControllerRepresentableContext<MailView>
    ) -> MFMailComposeViewController {
        let vc = MFMailComposeViewController()
        vc.mailComposeDelegate = context.coordinator
        vc.setToRecipients([toEmail])
        vc.setSubject(subject)
        if let body = body {
            vc.setMessageBody(body, isHTML: false)
        }
        return vc
    }
    
    func updateUIViewController(
        _ uiViewController: MFMailComposeViewController,
        context: UIViewControllerRepresentableContext<MailView>
    ) {
    }
}
