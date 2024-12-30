import SwiftUI
import Combine
import shared

struct VmView<VmState: AnyObject, Vm: __Vm<VmState>, Content: View>: View {
    
    @StateObject private var swiftVm: SwiftVm<VmState, Vm>
    @ViewBuilder private let content: (Vm, VmState) -> Content
    
    init(
        _ buildVm: @escaping () -> Vm,
        content: @escaping (Vm, VmState) -> Content
    ) {
        _swiftVm = StateObject(wrappedValue: SwiftVm(buildVm: buildVm))
        self.content = content
    }
    
    var body: some View {
        content(swiftVm.vm, swiftVm.vm.state.value as! VmState)
            .onAppear {
                swiftVm.vm.onAppear()
            }
            .onReceive(swiftVm.publisher) { newState in
                swiftVm.state = newState
            }
    }
}

///

private class SwiftVm<VmState: AnyObject, Vm: __Vm<VmState>>: ObservableObject {
    
    let vm: Vm
    @Published var state: VmState
    let publisher: AnyPublisher<VmState, Never>
    
    init(buildVm: () -> Vm) {
        let vm = buildVm()
        self.vm = vm
        self.state = vm.state.value as! VmState
        self.publisher = vm.state.toPublisher()
    }
    
    deinit {
        vm.onDisappear()
    }
}
