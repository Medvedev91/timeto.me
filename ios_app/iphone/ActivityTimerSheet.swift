import SwiftUI
import shared

extension NativeSheet {
    
    func showActivityTimerSheet(
        activity: ActivityDb,
        timerContext: ActivityTimerSheetVm.TimerContext?,
        // Set false for nested sheet to speed up closing
        hideOnStart: Bool,
        onStart: @escaping () -> Void
    ) {
        self.show { isPresented in
            VmView({
                ActivityTimerSheetVm(
                    activity: activity,
                    timerContext: timerContext
                )
            }) { vm, state in
                ActivityTimerSheet(
                    vm: vm,
                    state: state,
                    timerContext: timerContext,
                    onStart: {
                        if hideOnStart {
                            isPresented.wrappedValue = false
                        }
                        onStart()
                    }
                )
            }
        }
    }
}

private struct ActivityTimerSheet: View {
    
    let vm: ActivityTimerSheetVm
    let state: ActivityTimerSheetVm.State
    
    let timerContext: ActivityTimerSheetVm.TimerContext?
    let onStart: () -> ()
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    
    @State private var formTimeItemsIdx: Int32 = 0.toInt32()
    
    var body: some View {
        
        VStack {
            
            HStack(spacing: 4) {
                
                Button("Cancel") {
                    dismiss()
                }
                
                Spacer()
                
                Text(state.title)
                    .font(.title2)
                    .fontWeight(.semibold)
                    .multilineTextAlignment(.center)
                
                Spacer()
                
                Button("Start") {
                    vm.start {
                        onStart()
                    }
                }
                .fontWeight(.bold)
            }
            .padding(.horizontal, 25)
            .padding(.top, 24)
            
            // Plus padding from picker
            if let note = state.note {
                Text(note)
                    .font(.system(size: 14))
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.top, 6)
            }
            
            ZStack {
                
                VStack {
                    
                    Spacer()
                    
                    Picker(
                        "Time",
                        selection: $formTimeItemsIdx
                    ) {
                        ForEach(state.timeItems, id: \.idx) { item in
                            Text(item.title)
                        }
                    }
                    .onChange(of: formTimeItemsIdx) { newValue in
                        vm.setFormTimeItemIdx(newIdx: newValue)
                    }
                    .onAppear {
                        formTimeItemsIdx = state.formTimeItemIdx
                    }
                    .pickerStyle(.wheel)
                    .foregroundColor(.primary)
                    .padding(.bottom, state.note != nil ? 30 : 5)
                    
                    Spacer()
                }
                
                VStack {
                    
                    Spacer()
                    
                    HStack {
                        TimerHintsView(
                            timerHintsUI: state.timerHints,
                            hintHPadding: 8.0,
                            fontSize: 16.0,
                            fontWeight: .light,
                            onStart: {
                                onStart()
                            }
                        )
                    }
                    .padding(.bottom, 8)
                }
                .safeAreaPadding(.bottom)
            }
        }
        .presentationDetents([.height(400)])
        .presentationDragIndicator(.visible)
        .ignoresSafeArea()
    }
}
