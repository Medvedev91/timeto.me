import SwiftUI
import Combine
import shared

let roundedShape = RoundedRectangle(cornerRadius: 99, style: .circular)
let squircleShape = RoundedRectangle(cornerRadius: 12, style: .continuous)

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

    private let vm: __Vm<VMState>
    @State private var state: VMState
    private let publisher: AnyPublisher<VMState, Never>
    @ViewBuilder private let content: (VMState) -> Content
    private let stack: StackType

    init(
        vm: __Vm<VMState>,
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
                ZStack(alignment: p1) {
                    content(state)
                }
            case .VStack(let p1, let p2):
                VStack(alignment: p1, spacing: p2) {
                    content(state)
                }
            case .HStack(let p1, let p2):
                HStack(alignment: p1, spacing: p2) {
                    content(state)
                }
            }
        }
            // In onAppear() because init() is called frequently even the
            // view is not showed. "Unnecessary" calls is 90% times.
            // Yes, onAppear() calls too late but the default values DI saves.
                .onAppear {
                    vm.onAppear()
                }
                .onDisappear {
                    vm.onDisappear()
                }
                ////
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

///
/// Custom HStack/VStack/Spacer for default spacing

struct HStack<Content: View>: View {

    var alignment: VerticalAlignment = .center
    var spacing: CGFloat? = 0
    @ViewBuilder let content: () -> Content

    var body: some View {
        SwiftUI.HStack(alignment: alignment, spacing: spacing, content: content)
    }
}

struct VStack<Content: View>: View {

    var alignment: HorizontalAlignment = .center
    var spacing: CGFloat? = 0
    @ViewBuilder let content: () -> Content

    var body: some View {
        SwiftUI.VStack(alignment: alignment, spacing: spacing, content: content)
    }
}

struct Spacer: View {

    var body: some View {
        SwiftUI.Spacer(minLength: 0)
    }
}

