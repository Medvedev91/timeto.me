import SwiftUI

struct SheetHeaderView: View {

    let onCancel: () -> Void
    let title: String
    let doneText: String
    let isDoneEnabled: Bool
    let scrollToHeader: Int
    let onDone: () -> Void

    @State private var bg = UIColor.myDayNightArgb(0xFFF9F9F9, 0xFF191919)
    @State private var bgDivider = UIColor.myDayNightArgb(0xFFE9E9E9, 0xFF1F1F1F)

    private var bgAlpha: Double {
        (Double(scrollToHeader) / 30).min(1.0).max(0.0)
    }

    var body: some View {

        ZStack(alignment: .bottom) { // .bottom for divider

            HStack {

                Button(
                        action: {
                            onCancel()
                        },
                        label: { Text("Cancel") }
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
                        }
                )
                        .disabled(!isDoneEnabled)
            }
                    .padding(.top, 18)
                    .padding(.bottom, 18)

            Color(bgDivider)
                    .opacity(bgAlpha)
                    .frame(height: 1)
        }
                .background(Color(bg).opacity(bgAlpha))
    }
}
