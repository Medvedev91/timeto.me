import SwiftUI
import Combine
import shared

struct VmView<
    VmStateT: AnyObject,
    VmT: Vm<VmStateT>,
    Content: View
>: View {
    
    @StateObject private var swiftVm: SwiftVm<VmStateT, VmT>
    @ViewBuilder private let content: (VmT, VmStateT) -> Content
    
    init(
        _ buildVm: @escaping () -> VmT,
        @ViewBuilder content: @escaping (VmT, VmStateT) -> Content
    ) {
        _swiftVm = StateObject(wrappedValue: SwiftVm(buildVm: buildVm))
        self.content = content
    }
    
    var body: some View {
        content(swiftVm.vm, swiftVm.vm.state.value as! VmStateT)
            .onReceive(swiftVm.publisher) { newState in
                swiftVm.state = newState
            }
    }
}

///

private class SwiftVm<
    VmStateT: AnyObject,
    VmT: Vm<VmStateT>
>: ObservableObject {
    
    let vm: VmT
    @Published var state: VmStateT
    let publisher: AnyPublisher<VmStateT, Never>
    
    init(buildVm: () -> VmT) {
        let vm = buildVm()
        self.vm = vm
        self.state = vm.state.value as! VmStateT
        self.publisher = vm.state.toPublisher()
    }
    
    deinit {
        vm.onDestroy()
    }
}
