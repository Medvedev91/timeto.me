import SwiftUI
import Combine
import shared

extension Kotlinx_coroutines_coreFlow {

    func toPublisher<T: AnyObject>() -> AnyPublisher<T, Never> {
        let swiftFlow = SwiftFlow<T>(kotlinFlow: self)
        return Deferred<Publishers.HandleEvents<PassthroughSubject<T, Never>>> {
            let subject = PassthroughSubject<T, Never>()
            let cancelable = swiftFlow.watch { next in
                if let next = next {
                    subject.send(next)
                }
            }
            return subject.handleEvents(receiveCancel: {
                cancelable.cancel()
            })
        }
                .eraseToAnyPublisher()
    }
}

struct VMView<VMState: AnyObject, Content: View>: View {

    private let vm: __VM<VMState>
    @State private var state: VMState
    private let publisher: AnyPublisher<VMState, Never>
    @ViewBuilder private let content: (VMState) -> Content
    private let stack: StackType

    init(
            vm: __VM<VMState>,
            stack: StackType = .ZStack(),
            @ViewBuilder content: @escaping (VMState) -> Content
    ) {
        self.vm = vm
        state = vm.state.value as! VMState
        publisher = vm.state.toPublisher()
        self.stack = stack
        self.content = content
    }

    var body: some View {
        ZStack {
            switch stack {
            case .ZStack(let p1):
                ZStack(alignment: p1) { content(state) }
            case .VStack(let p1, let p2):
                VStack(alignment: p1, spacing: p2) { content(state) }
            case .HStack(let p1, let p2):
                HStack(alignment: p1, spacing: p2) { content(state) }
            }
        }
                /// In onAppear() because init() is called frequently even the
                /// view is not showed. "Unnecessary" calls is 90% times.
                /// Yes, onAppear() calls too late but the default values DI saves.
                .onAppear {
                    vm.onAppear()
                }
                .onDisappear {
                    vm.onDisappear()
                }
                //////
                .onReceive(publisher) { res in
                    state = res
                }
    }

    enum StackType {
        case ZStack(alignment: Alignment = .center)
        case VStack(alignment: HorizontalAlignment = .center, spacing: CGFloat? = 0)
        case HStack(alignment: VerticalAlignment = .center, spacing: CGFloat? = 0)
    }
}
