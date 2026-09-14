import SwiftUI
import MessageUI

struct AskQuestionView<Content: View>: View {
    
    let subject: String
    @ViewBuilder let content: () -> Content
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    @State private var mailViewResult: Result<MFMailComposeResult, Error>? = nil
    
    var body: some View {
        Button(
            action: {
                AskQuestionUtils.sendEmail(
                    navigation: navigation,
                    subject: subject,
                    mailViewResult: $mailViewResult,
                )
            },
            label: {
                content()
            },
        )
    }
}
