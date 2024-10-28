import SwiftUI
import shared

struct GoalPeriodFormFs: View {
    
    @State private var vm: GoalPeriodFormVm
    @Binding private var isPresented: Bool
    private let onSelect: (GoalDbPeriod) -> ()
    
    @State private var fsHeaderScroll = 0

    init(
        isPresented: Binding<Bool>,
        initPeriod: GoalDbPeriod?,
        onSelect: @escaping (GoalDbPeriod) -> ()
    ) {
        _isPresented = isPresented
        self.onSelect = onSelect
        vm = GoalPeriodFormVm(initPeriod: initPeriod)
    }
    
    var body: some View {
        
        VMView(vm: vm, stack: .VStack()) { state in
            
            Fs__HeaderAction(
                title: state.headerTitle,
                actionText: state.headerDoneText,
                scrollToHeader: fsHeaderScroll,
                onCancel: {
                    isPresented = false
                },
                onDone: {
                    vm.buildPeriod { period in
                        onSelect(period)
                        isPresented = false
                    }
                }
            )
            
            ScrollViewWithVListener(showsIndicators: false, vScroll: $fsHeaderScroll) {
                
                VStack {
                    
                }
            }
        }
    }
}
