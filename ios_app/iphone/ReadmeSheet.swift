import SwiftUI

struct ReadmeSheet: View {

    @Binding var isPresented: Bool

    @State private var scroll = 0

    var body: some View {

        VStack {

            Sheet__HeaderView(
                    title: "How to Use",
                    scrollToHeader: scroll,
                    bgColor: Color(.bg)
            )

            ScrollViewWithVListener(showsIndicators: false, vScroll: $scroll) {

                VStack {

                    Text("Set a timer for each task to stay focused.")
                            .fontWeight(.bold)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.bottom, 8)

                    Text("No \"stop\" option is the main feature of this app. Once you have completed one activity, you have to set a timer for the next one, even if it's a \"sleeping\" activity.")
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.bottom, 8)

                    // todo
                    //                                Text("This time-tracking approach provides real 24/7 data on how long everything takes. You can see it on the ")
                    //                                        + Text("Chart").fontWeight(.bold)
                    //                                        + Text(".")
                    Text("This time-tracking approach provides real 24/7 data on how long everything takes. You can see it on the Chart.")
                            .frame(maxWidth: .infinity, alignment: .leading)

                    Spacer()
                }
                        .padding(.horizontal, 20)
                        .padding(.vertical, 16)
            }

            Sheet__BottomViewClose {
                isPresented = false
            }
        }
    }
}
