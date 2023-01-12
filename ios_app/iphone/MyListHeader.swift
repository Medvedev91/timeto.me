import SwiftUI

struct MyListHeader: View {

    let title: String
    var rightView: AnyView? = nil

    var body: some View {

        HStack {

            Text(title)
                    .foregroundColor(.primary.opacity(0.55))
                    .fontWeight(.regular)
                    .textCase(.uppercase)
                    .font(.system(size: 13.5))

            Spacer(minLength: 0)

            if let rightView = rightView {
                rightView
            }
        }
                .padding(.horizontal, 36)
                .padding(.top, 25)
    }
}
