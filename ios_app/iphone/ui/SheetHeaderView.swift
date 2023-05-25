import SwiftUI

struct SheetHeaderView: View {

    let onCancel: () -> Void
    let title: String
    let doneText: String
    let isDoneEnabled: Bool?
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

            HStack {

                Button(
                        action: {
                            onCancel()
                        },
                        label: { Text(cancelText) }
                )
                        .padding(.leading, 25)

                Spacer()

                Text(title)
                        .font(.title3)
                        .fontWeight(.semibold)
                        .multilineTextAlignment(.center)

                Spacer()

                Button(
                        action: {
                            onDone()
                        },
                        label: {
                            Text(doneText)
                                    .fontWeight(.bold)
                                    .padding(.trailing, 25)
                                    .opacity(isDoneEnabled == nil ? 0 : 1)
                        }
                )
                        .disabled(isDoneEnabled != true)
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
