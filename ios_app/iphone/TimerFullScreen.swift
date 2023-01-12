import SwiftUI
import shared

var isTimerFullScreenPresentedGlobal = false

extension View {

    func attachTimerFullScreenView() -> some View {
        modifier(TimerFullScreen__ViewModifier())
    }
}

class TimerFullScreen: ObservableObject {
    @Published var isPresented = false
}

///
///

private struct TimerFullScreen__ViewModifier: ViewModifier {

    @StateObject private var timerFullScreen = TimerFullScreen()

    func body(content: Content) -> some View {

        content
                /// Скрывание status bar в .statusBar(...)
                .fullScreenCover(isPresented: $timerFullScreen.isPresented) {
                    TimerFullScreen__FullScreenCoverView()
                }
                .onChange(of: timerFullScreen.isPresented) { newValue in
                    isTimerFullScreenPresentedGlobal = newValue
                }
                .environmentObject(timerFullScreen)
    }
}

private struct TimerFullScreen__FullScreenCoverView: View {

    @State private var vm = FullscreenVM(defColor: ColorNative.white)

    @EnvironmentObject private var timerFullScreen: TimerFullScreen

    var body: some View {

        VMView(vm: vm, stack: .ZStack()) { state in

            let timerData = state.timerData

            Color.black.edgesIgnoringSafeArea(.all)
                    .statusBar(hidden: true)

            VStack {

                Text(state.title)
                        .foregroundColor(.white)
                        .font(.system(size: 22, weight: .light))
                        .tracking(0.4)
                        .padding(.top, 35)
                        .padding(.leading, 20)
                        .padding(.trailing, 20)
                        .opacity(0.9)
                        .multilineTextAlignment(.center)

                Spacer()
                Button(
                        action: {
                            timerFullScreen.isPresented = false
                        },
                        label: {
                            Image(systemName: "xmark")
                                    .font(.system(size: 26, weight: .thin))
                                    .foregroundColor(.white)
                        }
                )
                        .padding(.bottom, 30)
                        .opacity(0.8)
            }

            VStack {

                Text(timerData.title ?? "")
                        .font(.system(size: 26, weight: .bold))
                        .tracking(5)
                        .foregroundColor(timerData.color.toColor())
                        .opacity(0.9)
                        .padding(.bottom, -10)

                Text(timerData.timer)
                        .font(.system(size: 72, design: .monospaced))
                        .foregroundColor(timerData.color.toColor())
                        .opacity(0.9)

                Button(
                        action: {
                            IntervalModel.Companion().restartActualInterval { _ in
                                // todo
                            }
                        },
                        label: {
                            Text("Restart")
                                    .font(.system(size: 25, weight: .light))
                                    .foregroundColor(.white)
                                    .tracking(1)
                        }
                )
                        ///
                        .opacity(timerData.title == nil ? 0 : 1)
                        .disabled(timerData.title == nil)
                        ///
                        .padding(.top, -15)
            }
                    .padding(.bottom, 20)
        }
                .onAppear {
                    UIApplication.shared.isIdleTimerDisabled = true
                }
                .onDisappear {
                    UIApplication.shared.isIdleTimerDisabled = false
                }
    }
}
