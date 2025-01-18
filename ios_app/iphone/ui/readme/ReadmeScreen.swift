import SwiftUI
import shared

struct ReadmeScreen: View {
    
    let defaultItem: ReadmeVm.DefaultItem
    
    var body: some View {
        VmView({
            ReadmeVm(defaultItem: defaultItem)
        }) { vm, state in
            ReadmeScreenInner(
                vm: vm,
                state: state,
                selectedTab: state.tabUi // @State inited once
            )
        }
    }
}

private struct ReadmeScreenInner: View {
    
    let vm: ReadmeVm
    let state: ReadmeVm.State
    
    @State var selectedTab: ReadmeVm.TabUi
    
    var body: some View {
        
        ZStack {
            ReadmeTabView(tabUi: state.tabUi)
                .id("tab_\(state.tabUi.id)") // To scroll to top
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .principal) {
                Picker("", selection: $selectedTab) {
                    ForEach(state.tabsUi, id: \.id) { tabUi in
                        Text(tabUi.title)
                            .tag(tabUi)
                    }
                }
                .pickerStyle(.segmented)
            }
        }
        .onChange(of: selectedTab) { _, new in
            vm.setTabUi(tabUi: new)
        }
    }
}
