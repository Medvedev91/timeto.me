import SwiftUI

struct SheetHeaderView: View {

    let onCancel: () -> Void
    let title: String
    let doneText: String?
    let isDoneEnabled: Bool
    let scrollToHeader: Int
    var cancelText: String = "Cancel"
    var bgColor = UIColor.formHeaderBackground
    var dividerColor = UIColor.formHeaderDivider
    let onDone: () -> Void

    private var bgAlpha: Double {
        (Double(scrollToHeader) / 30).limitMinMax(0, 1)
    }

    var body: some View {

        ZStack(alignment: .bottom) { // .bottom for divider

            ZStack {

                // Cancel Button
                HStack {
                    Button(
                            action: {
                                onCancel()
                            },
                            label: {
                                Text(cancelText)
                                        .font(.system(size: 17))
                            }
                    )
                            .padding(.leading, 25)
                    Spacer()
                }

                // Done Button
                if let doneText = doneText {
                    HStack {
                        Spacer()
                        Button(
                                action: {
                                    onDone()
                                },
                                label: {
                                    Text(doneText)
                                            .font(.system(size: 17, weight: .bold))
                                }
                        )
                                .disabled(!isDoneEnabled)
                                .padding(.trailing, 27)
                    }
                }

                // Title on the end to place on the top
                Text(title)
                        .font(.title3)
                        .fontWeight(.semibold)
                        .multilineTextAlignment(.center)
            }
                    .padding(.top, 18)
                    .padding(.bottom, 18)

            Color(dividerColor)
                    .opacity(bgAlpha)
                    .frame(height: onePx)
        }
                .background(Color(bgColor).opacity(bgAlpha))
    }
}
