import SwiftUI
import Combine
import shared

extension View {

    func attachTimerFullScreenView() -> some View {
        modifier(TimerFullScreen__ViewModifier())
    }
}

///
///

private struct TimerFullScreen__ViewModifier: ViewModifier {

    @State private var isPresented = false

    private let statePublisher: AnyPublisher<KotlinBoolean, Never> = FullScreenUI.shared.state.toPublisher()

    func body(content: Content) -> some View {

        content
                /// Скрывание status bar в .statusBar(...)
                .fullScreenCover(isPresented: $isPresented) {
                    TimerFullScreen__FullScreenCoverView()
                }
                .onReceive(statePublisher) { newValue in
                    isPresented = newValue.boolValue
                }
    }
}

private struct TimerFullScreen__FullScreenCoverView: View {

    @State private var vm = FullscreenVM(defColor: .white)

    var body: some View {

        VMView(vm: vm, stack: .ZStack()) { state in

            Color.black.edgesIgnoringSafeArea(.all)
                    .statusBar(hidden: true)

            if let checklistUI = state.checklistUI {
                VStack {
                    TimerFullScreen__HeaderView(state: state)
                    TimerFullScreen__TimerView(vm: vm, state: state, isCompact: true)
                            .padding(.top, 20)
                    Spacer()
                    TimerFullScreen__CloseView()
                }
            } else {
                VStack {
                    TimerFullScreen__HeaderView(state: state)
                    Spacer()
                    TimerFullScreen__CloseView()
                }
                TimerFullScreen__TimerView(vm: vm, state: state, isCompact: false)
            }
        }
                .onAppear {
                    UIApplication.shared.isIdleTimerDisabled = true
                }
                .onDisappear {
                    UIApplication.shared.isIdleTimerDisabled = false
                }
    }
}

private struct TimerFullScreen__HeaderView: View {

    let state: FullscreenVM.State

    var body: some View {

        Text(state.title)
                .foregroundColor(.white)
                .font(.system(size: 22, weight: .light))
                .tracking(0.4)
                .padding(.top, 35)
                .padding(.leading, 20)
                .padding(.trailing, 20)
                .opacity(0.9)
                .multilineTextAlignment(.center)
    }
}


private struct TimerFullScreen__TimerView: View {

    let vm: FullscreenVM
    let state: FullscreenVM.State
    let isCompact: Bool

    var body: some View {

        VStack(spacing: 0) {

            let timerData = state.timerData

            Text(timerData.title ?? "")
                    .font(.system(size: 26, weight: .bold))
                    .tracking(5)
                    .foregroundColor(timerData.color.toColor())
                    .opacity(0.9)
                    .padding(.bottom, isCompact ? 0 : 20)

            Text(timerData.timer)
                    .font(.system(size: 72, design: .monospaced))
                    .foregroundColor(timerData.color.toColor())
                    .opacity(0.9)

            Button(
                    action: {
                        vm.restart()
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
                    .padding(.top, isCompact ? 5 : 20)
        }
                .padding(.bottom, 20)
    }
}

private struct TimerFullScreen__CloseView: View {

    var body: some View {

        Button(
                action: {
                    FullScreenUI.shared.close()
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
}
