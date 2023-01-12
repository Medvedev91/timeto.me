import SwiftUI

struct MyListItem_Button: View {

    let text: String
    let withTopDivider: Bool
    var rightView: AnyView? = nil
    let onClick: () -> Void

    var body: some View {

        ZStack(alignment: .top) {

            HStack {

                Button(
                        action: {
                            onClick()
                        },
                        label: {

                            HStack {

                                Text(text)
                                        .padding(.leading, DEF_LIST_H_PADDING)
                                        .padding(.vertical, DEF_LIST_V_PADDING)

                                Spacer(minLength: 0)

                                if let rightView = rightView {
                                    rightView
                                }
                            }
                                    .frame(maxWidth: .infinity)
                        }
                )
                        .foregroundColor(.primary)
            }

            if withTopDivider {
                MyDivider(xOffset: 20)
            }
        }
                .frame(maxWidth: .infinity)
                .background(Color(.mySecondaryBackground))
    }
}
